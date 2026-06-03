package com.example.javademo.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * v0.1 登录闭环集成测试。
 *
 * <p>测试使用随机端口启动完整 Spring Boot 应用，并通过 HTTP 调用真实接口。
 * 数据库使用 test profile 下的 H2 内存库，既能覆盖 Controller、Service、Mapper、拦截器，
 * 又不依赖本机 Docker MySQL，适合作为每次改动后的快速回归测试。</p>
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterLoginAndReadCurrentUserWithJwt() throws Exception {
        // 使用 Apache HttpClient 请求工厂，确保 4xx 响应可以被 TestRestTemplate 正常接收和断言。
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        Map<String, String> registerRequest = Map.of(
                "username", "alice",
                "password", "secret123",
                "nickname", "Alice"
        );

        // 1. 注册新用户，验证接口返回成功且响应中不包含密码信息。
        ResponseEntity<String> registerResponse = restTemplate.postForEntity("/api/auth/register", registerRequest, String.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode registerJson = readJson(registerResponse);
        assertThat(registerJson.path("code").asInt()).isZero();
        assertThat(registerJson.path("data").path("username").asText()).isEqualTo("alice");

        // 2. 再次注册同名用户，验证唯一约束和冲突响应是否生效。
        ResponseEntity<String> duplicateResponse = restTemplate.postForEntity("/api/auth/register", registerRequest, String.class);
        assertThat(duplicateResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        Map<String, String> loginRequest = Map.of(
                "username", "alice",
                "password", "secret123"
        );
        // 3. 使用正确密码登录，拿到后续接口访问所需的 JWT。
        ResponseEntity<String> loginResponse = restTemplate.postForEntity("/api/auth/login", loginRequest, String.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode loginJson = readJson(loginResponse);
        String token = loginJson.path("data").path("accessToken").asText();
        assertThat(token).isNotBlank();

        // 4. 带上 Bearer token 访问当前用户接口，验证认证拦截器和用户上下文正常工作。
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> meResponse = restTemplate.exchange(
                "/api/users/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode meJson = readJson(meResponse);
        assertThat(meJson.path("data").path("username").asText()).isEqualTo("alice");

        // 5. 不带 token 访问受保护接口，应被拦截器拦截并返回 401。
        ResponseEntity<String> noTokenResponse = restTemplate.getForEntity("/api/users/me", String.class);
        assertThat(noTokenResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        Map<String, String> badLoginRequest = Map.of(
                "username", "alice",
                "password", "wrong-password"
        );
        // 6. 使用错误密码登录，应返回 401，且不应签发 token。
        ResponseEntity<String> badLoginResponse = restTemplate.postForEntity("/api/auth/login", badLoginRequest, String.class);
        assertThat(badLoginResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // 7. 验证 OpenAPI JSON 可生成，避免后续修改接口时意外破坏 Swagger 文档。
        ResponseEntity<String> openApiResponse = restTemplate.getForEntity("/v3/api-docs", String.class);
        assertThat(openApiResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode openApiJson = readJson(openApiResponse);
        assertThat(openApiJson.path("info").path("title").asText()).isEqualTo("Java Demo API");
        assertThat(openApiJson.path("paths").has("/api/auth/register")).isTrue();
        assertThat(openApiJson.path("paths").has("/api/auth/login")).isTrue();
        assertThat(openApiJson.path("paths").has("/api/auth/captcha/slider")).isTrue();
        assertThat(openApiJson.path("paths").has("/api/auth/captcha/slider/verify")).isTrue();
        assertThat(openApiJson.path("paths").has("/api/users/me")).isTrue();
        assertThat(openApiJson.path("paths").has("/api/users")).isTrue();
        assertThat(openApiJson.path("paths").has("/api/users/{id}")).isTrue();
        assertThat(openApiJson.path("paths").has("/api/users/{id}/password")).isTrue();
        assertThat(openApiJson.path("components").path("securitySchemes").has("bearerAuth")).isTrue();
    }

    /**
     * v0.5.4 登录滑块验证码集成测试。
     *
     * <p>该测试覆盖“失败 3 次 -> 必须验证码 -> challenge/verify -> 带 captchaToken 登录成功”的核心闭环。
     * 测试断言不会读取或依赖后端内部验证码答案，只根据接口返回的 UI 尺寸把滑块拖到终点，
     * 与当前学习型 MVP 前端交互保持一致。</p>
     */
    @Test
    void shouldRequireSliderCaptchaAfterRepeatedLoginFailures() throws Exception {
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        String username = "risk_" + System.nanoTime();
        String password = "secret123";
        Map<String, String> registerRequest = Map.of(
                "username", username,
                "password", password,
                "nickname", "Risk User"
        );
        ResponseEntity<String> registerResponse = restTemplate.postForEntity("/api/auth/register", registerRequest, String.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, String> badLoginRequest = Map.of(
                "username", username,
                "password", "wrong-password"
        );

        // 前两次失败只返回普通 401，不强制前端展示验证码。
        assertThat(restTemplate.postForEntity("/api/auth/login", badLoginRequest, String.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(restTemplate.postForEntity("/api/auth/login", badLoginRequest, String.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        // 第三次失败进入风险状态，响应 code=4601，并携带 captchaRequired=true 供前端联动。
        ResponseEntity<String> thirdFailureResponse = restTemplate.postForEntity("/api/auth/login", badLoginRequest, String.class);
        assertThat(thirdFailureResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        JsonNode thirdFailureJson = readJson(thirdFailureResponse);
        assertThat(thirdFailureJson.path("code").asInt()).isEqualTo(4601);
        assertThat(thirdFailureJson.path("data").path("captchaRequired").asBoolean()).isTrue();

        Map<String, String> goodLoginWithoutCaptchaRequest = Map.of(
                "username", username,
                "password", password
        );
        ResponseEntity<String> goodWithoutCaptchaResponse = restTemplate.postForEntity(
                "/api/auth/login",
                goodLoginWithoutCaptchaRequest,
                String.class
        );
        assertThat(goodWithoutCaptchaResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(readJson(goodWithoutCaptchaResponse).path("code").asInt()).isEqualTo(4601);

        // 错误滑块位置应返回明确验证码错误，并消耗当前 challenge，前端需要重新获取。
        ResponseEntity<String> wrongChallengeResponse = restTemplate.postForEntity(
                "/api/auth/captcha/slider",
                Map.of("username", username),
                String.class
        );
        assertThat(wrongChallengeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String wrongChallengeId = readJson(wrongChallengeResponse).path("data").path("challengeId").asText();
        ResponseEntity<String> wrongVerifyResponse = restTemplate.postForEntity(
                "/api/auth/captcha/slider/verify",
                Map.of("challengeId", wrongChallengeId, "sliderPosition", 0),
                String.class
        );
        assertThat(wrongVerifyResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(readJson(wrongVerifyResponse).path("code").asInt()).isEqualTo(4602);

        // 重新获取 challenge，并按当前 MVP 规则拖到终点完成验证。
        ResponseEntity<String> challengeResponse = restTemplate.postForEntity(
                "/api/auth/captcha/slider",
                Map.of("username", username),
                String.class
        );
        assertThat(challengeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode challengeJson = readJson(challengeResponse).path("data");
        String challengeId = challengeJson.path("challengeId").asText();
        int sliderPosition = challengeJson.path("trackWidth").asInt() - challengeJson.path("puzzleWidth").asInt();

        ResponseEntity<String> verifyResponse = restTemplate.postForEntity(
                "/api/auth/captcha/slider/verify",
                Map.of("challengeId", challengeId, "sliderPosition", sliderPosition),
                String.class
        );
        assertThat(verifyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String captchaToken = readJson(verifyResponse).path("data").path("captchaToken").asText();
        assertThat(captchaToken).isNotBlank();

        ResponseEntity<String> goodWithCaptchaResponse = restTemplate.postForEntity(
                "/api/auth/login",
                Map.of("username", username, "password", password, "captchaToken", captchaToken),
                String.class
        );
        assertThat(goodWithCaptchaResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readJson(goodWithCaptchaResponse).path("data").path("accessToken").asText()).isNotBlank();

        // 登录成功会清理失败计数，后续同一用户同一 IP 可以恢复普通账号密码登录。
        ResponseEntity<String> goodAfterClearResponse = restTemplate.postForEntity(
                "/api/auth/login",
                goodLoginWithoutCaptchaRequest,
                String.class
        );
        assertThat(goodAfterClearResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /**
     * 将 HTTP 响应体解析为 JsonNode，方便测试直接断言嵌套字段。
     */
    private JsonNode readJson(ResponseEntity<String> response) throws Exception {
        return objectMapper.readTree(response.getBody());
    }
}
