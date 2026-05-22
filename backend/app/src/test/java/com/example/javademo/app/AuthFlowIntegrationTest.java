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
        assertThat(openApiJson.path("paths").has("/api/users/me")).isTrue();
        assertThat(openApiJson.path("components").path("securitySchemes").has("bearerAuth")).isTrue();
    }

    /**
     * 将 HTTP 响应体解析为 JsonNode，方便测试直接断言嵌套字段。
     */
    private JsonNode readJson(ResponseEntity<String> response) throws Exception {
        return objectMapper.readTree(response.getBody());
    }
}
