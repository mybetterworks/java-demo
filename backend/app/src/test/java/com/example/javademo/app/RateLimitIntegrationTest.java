package com.example.javademo.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * v0.7 基础限流集成测试。
 *
 * <p>测试 profile 默认关闭 Redis，这里只单独开启限流，用内存降级路径验证过滤器行为。
 * 真实联调时 Redis 可用后，同一套 RateLimitService 会自动切换为 Redis INCR + EXPIRE。</p>
 */
@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "java-demo.rate-limit.enabled=true",
                "java-demo.rate-limit.login-limit=2",
                "java-demo.rate-limit.window-seconds=60"
        }
)
class RateLimitIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRejectLoginWhenRateLimitExceeded() throws Exception {
        Map<String, String> loginRequest = Map.of(
                "username", "rate_limit_user",
                "password", "wrong-password"
        );

        // 前两次请求进入登录业务本身，因为用户不存在而返回 401。
        assertThat(restTemplate.postForEntity("/api/auth/login", loginRequest, String.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(restTemplate.postForEntity("/api/auth/login", loginRequest, String.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        // 第三次在过滤器层被限流，返回统一响应结构和 429。
        ResponseEntity<String> limitedResponse = restTemplate.postForEntity("/api/auth/login", loginRequest, String.class);
        assertThat(limitedResponse.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        JsonNode json = objectMapper.readTree(limitedResponse.getBody());
        assertThat(json.path("code").asInt()).isEqualTo(429);
    }
}
