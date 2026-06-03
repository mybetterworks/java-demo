package com.example.javademo.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
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
     * v0.5.5 登录拼图验证码集成测试。
     *
     * <p>该测试覆盖“失败 3 次 -> 必须验证码 -> challenge/verify -> 带 captchaToken 登录成功”的核心闭环。
     * 测试不会从响应字段读取答案；为了让自动化测试能真实走完 verify，会把后端返回的带缺口背景图
     * 与项目内置原始背景图做像素差分，从图片结果中定位缺口位置。</p>
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

        // 错误滑块位置应返回明确验证码错误，不会签发 captchaToken。
        ResponseEntity<String> wrongChallengeResponse = restTemplate.postForEntity(
                "/api/auth/captcha/slider",
                Map.of("username", username),
                String.class
        );
        assertThat(wrongChallengeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String wrongChallengeId = readJson(wrongChallengeResponse).path("data").path("challengeId").asText();
        ResponseEntity<String> wrongVerifyResponse = restTemplate.postForEntity(
                "/api/auth/captcha/slider/verify",
                Map.of(
                        "challengeId", wrongChallengeId,
                        "sliderX", 0,
                        "durationMs", 1200,
                        "tracks", buildCaptchaTracks(0, 72, 1200)
                ),
                String.class
        );
        assertThat(wrongVerifyResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(readJson(wrongVerifyResponse).path("code").asInt()).isEqualTo(4602);

        // 耗时过短即使位置正确也应失败，避免脚本瞬间提交。
        JsonNode shortDurationChallenge = createCaptchaChallenge(username);
        int shortDurationX = solvePuzzleTargetX(shortDurationChallenge);
        ResponseEntity<String> shortDurationResponse = restTemplate.postForEntity(
                "/api/auth/captcha/slider/verify",
                Map.of(
                        "challengeId", shortDurationChallenge.path("challengeId").asText(),
                        "sliderX", shortDurationX,
                        "durationMs", 50,
                        "tracks", buildCaptchaTracks(shortDurationX, shortDurationChallenge.path("puzzleY").asInt(), 50)
                ),
                String.class
        );
        assertThat(shortDurationResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(readJson(shortDurationResponse).path("code").asInt()).isEqualTo(4602);

        // 轨迹点过少即使位置正确也应失败，避免“一步跳到答案”的极简脚本。
        JsonNode abnormalTrackChallenge = createCaptchaChallenge(username);
        int abnormalTrackX = solvePuzzleTargetX(abnormalTrackChallenge);
        ResponseEntity<String> abnormalTrackResponse = restTemplate.postForEntity(
                "/api/auth/captcha/slider/verify",
                Map.of(
                        "challengeId", abnormalTrackChallenge.path("challengeId").asText(),
                        "sliderX", abnormalTrackX,
                        "durationMs", 1200,
                        "tracks", List.of(
                                Map.of("x", 0, "y", abnormalTrackChallenge.path("puzzleY").asInt(), "t", 0),
                                Map.of("x", abnormalTrackX, "y", abnormalTrackChallenge.path("puzzleY").asInt(), "t", 1200)
                        )
                ),
                String.class
        );
        assertThat(abnormalTrackResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(readJson(abnormalTrackResponse).path("code").asInt()).isEqualTo(4602);

        // 重新获取 challenge，并从图片差分中定位缺口完成验证。响应中不得暴露 targetX 或旧版可推导答案字段。
        ResponseEntity<String> challengeResponse = restTemplate.postForEntity(
                "/api/auth/captcha/slider",
                Map.of("username", username),
                String.class
        );
        assertThat(challengeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode challengeJson = readJson(challengeResponse).path("data");
        String challengeId = challengeJson.path("challengeId").asText();
        assertThat(challengeJson.has("targetX")).isFalse();
        assertThat(challengeJson.has("answerOffset")).isFalse();
        assertThat(challengeJson.path("backgroundImage").asText()).startsWith("data:image/png;base64,");
        assertThat(challengeJson.path("puzzleImage").asText()).startsWith("data:image/png;base64,");
        assertThat(challengeJson.path("imageWidth").asInt()).isEqualTo(320);
        assertThat(challengeJson.path("imageHeight").asInt()).isEqualTo(160);
        assertThat(challengeJson.path("puzzleHeight").asInt()).isEqualTo(48);
        int sliderPosition = solvePuzzleTargetX(challengeJson);
        assertThat(sliderPosition).isGreaterThan(0);
        assertThat(sliderPosition).isLessThan(challengeJson.path("trackWidth").asInt() - challengeJson.path("puzzleWidth").asInt());

        ResponseEntity<String> verifyResponse = restTemplate.postForEntity(
                "/api/auth/captcha/slider/verify",
                Map.of(
                        "challengeId", challengeId,
                        "sliderX", sliderPosition,
                        "durationMs", 1280,
                        "tracks", buildCaptchaTracks(sliderPosition, challengeJson.path("puzzleY").asInt(), 1280)
                ),
                String.class
        );
        assertThat(verifyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String captchaToken = readJson(verifyResponse).path("data").path("captchaToken").asText();
        assertThat(captchaToken).isNotBlank();

        // token 是一次性的：先用错误密码消费掉 token，再尝试复用同一个 token，必须被拒绝。
        ResponseEntity<String> wrongPasswordWithTokenResponse = restTemplate.postForEntity(
                "/api/auth/login",
                Map.of("username", username, "password", "wrong-password", "captchaToken", captchaToken),
                String.class
        );
        assertThat(wrongPasswordWithTokenResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(readJson(wrongPasswordWithTokenResponse).path("code").asInt()).isEqualTo(4601);

        ResponseEntity<String> reusedTokenResponse = restTemplate.postForEntity(
                "/api/auth/login",
                Map.of("username", username, "password", password, "captchaToken", captchaToken),
                String.class
        );
        assertThat(reusedTokenResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(readJson(reusedTokenResponse).path("code").asInt()).isEqualTo(4601);

        // 重新完成一次拼图验证后，携带新的 captchaToken 登录成功。
        JsonNode finalChallengeJson = createCaptchaChallenge(username);
        int finalSliderPosition = solvePuzzleTargetX(finalChallengeJson);
        ResponseEntity<String> finalVerifyResponse = restTemplate.postForEntity(
                "/api/auth/captcha/slider/verify",
                Map.of(
                        "challengeId", finalChallengeJson.path("challengeId").asText(),
                        "sliderX", finalSliderPosition,
                        "durationMs", 1460,
                        "tracks", buildCaptchaTracks(finalSliderPosition, finalChallengeJson.path("puzzleY").asInt(), 1460)
                ),
                String.class
        );
        assertThat(finalVerifyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String finalCaptchaToken = readJson(finalVerifyResponse).path("data").path("captchaToken").asText();
        assertThat(finalCaptchaToken).isNotBlank();

        ResponseEntity<String> goodWithCaptchaResponse = restTemplate.postForEntity(
                "/api/auth/login",
                Map.of("username", username, "password", password, "captchaToken", finalCaptchaToken),
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

    /**
     * 创建验证码 challenge 并直接返回 data 节点，减少主测试中的重复 HTTP 样板代码。
     */
    private JsonNode createCaptchaChallenge(String username) throws Exception {
        ResponseEntity<String> challengeResponse = restTemplate.postForEntity(
                "/api/auth/captcha/slider",
                Map.of("username", username),
                String.class
        );
        assertThat(challengeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        return readJson(challengeResponse).path("data");
    }

    /**
     * 根据返回的带缺口背景图和项目内置原始背景图做像素差分，找到变化最大的横向窗口。
     *
     * <p>这不是从接口字段读取答案，而是模拟“看图找到缺口”的测试动作。真实生产系统仍需结合
     * 限流、设备指纹、行为分析或第三方验证码服务进一步增强。</p>
     */
    private int solvePuzzleTargetX(JsonNode challengeJson) throws Exception {
        BufferedImage original = loadOriginalCaptchaBackground();
        BufferedImage backgroundWithHole = decodeDataUrlImage(challengeJson.path("backgroundImage").asText());
        int puzzleWidth = challengeJson.path("puzzleWidth").asInt();
        int puzzleHeight = challengeJson.path("puzzleHeight").asInt();
        int puzzleY = challengeJson.path("puzzleY").asInt();
        int maxX = challengeJson.path("trackWidth").asInt() - puzzleWidth;

        long bestScore = Long.MIN_VALUE;
        int bestX = 0;
        for (int x = 0; x <= maxX; x++) {
            long score = 0;
            for (int dx = 0; dx < puzzleWidth; dx++) {
                for (int dy = 0; dy < puzzleHeight; dy++) {
                    int originalRgb = original.getRGB(x + dx, puzzleY + dy);
                    int challengeRgb = backgroundWithHole.getRGB(x + dx, puzzleY + dy);
                    score += colorDistance(originalRgb, challengeRgb);
                }
            }
            if (score > bestScore) {
                bestScore = score;
                bestX = x;
            }
        }
        return bestX;
    }

    /** 构造一组基础拖动轨迹，覆盖起点、中间抖动和终点。 */
    private List<Map<String, Object>> buildCaptchaTracks(int sliderX, int puzzleY, long durationMs) {
        int firstStep = Math.max(8, sliderX / 4);
        int secondStep = Math.max(firstStep + 6, sliderX / 2);
        int thirdStep = Math.max(secondStep + 6, sliderX - 10);
        return List.of(
                Map.of("x", 0, "y", puzzleY, "t", 0),
                Map.of("x", firstStep, "y", puzzleY + 1, "t", Math.max(80, durationMs / 5)),
                Map.of("x", secondStep, "y", puzzleY, "t", Math.max(160, durationMs / 2)),
                Map.of("x", thirdStep, "y", puzzleY + 2, "t", Math.max(240, durationMs - 180)),
                Map.of("x", sliderX, "y", puzzleY + 1, "t", durationMs)
        );
    }

    /** 加载测试使用的原始验证码背景图。 */
    private BufferedImage loadOriginalCaptchaBackground() throws Exception {
        try (InputStream inputStream = new ClassPathResource("captcha/backgrounds/login-bg-01.png").getInputStream()) {
            return ImageIO.read(inputStream);
        }
    }

    /** 解码后端返回的 PNG data URL。 */
    private BufferedImage decodeDataUrlImage(String dataUrl) throws Exception {
        String base64 = dataUrl.substring(dataUrl.indexOf(',') + 1);
        byte[] bytes = Base64.getDecoder().decode(base64);
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    /** 计算两个 RGB 像素的颜色距离，用于寻找缺口造成的最大差异区域。 */
    private int colorDistance(int firstRgb, int secondRgb) {
        int firstRed = (firstRgb >> 16) & 0xff;
        int firstGreen = (firstRgb >> 8) & 0xff;
        int firstBlue = firstRgb & 0xff;
        int secondRed = (secondRgb >> 16) & 0xff;
        int secondGreen = (secondRgb >> 8) & 0xff;
        int secondBlue = secondRgb & 0xff;
        return Math.abs(firstRed - secondRed) + Math.abs(firstGreen - secondGreen) + Math.abs(firstBlue - secondBlue);
    }
}
