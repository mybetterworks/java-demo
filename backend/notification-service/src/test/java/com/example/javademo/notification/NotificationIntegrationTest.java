package com.example.javademo.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * notification-service 集成测试。
 *
 * <p>测试会在随机端口启动完整 Spring 容器，并使用 H2 初始化 notification_message 表。
 * 这样既能验证 Controller、JWT 拦截器、MyBatis Plus 和 SQL 初始化是否协作正常，又不会依赖本机 MySQL。</p>
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NotificationIntegrationTest {

    private static final String TEST_SECRET = "java-demo-test-secret-change-me-32chars";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldManageMyNotificationsWithJwt() throws Exception {
        // 1. 健康检查是公开接口，便于 Gateway 和后续注册中心探测。
        ResponseEntity<String> healthResponse = restTemplate.getForEntity("/api/notifications/health", String.class);
        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readJson(healthResponse).path("data").path("service").asText()).isEqualTo("notification-service");

        // 2. 业务接口必须携带 JWT，直连服务时也不能绕过认证防线。
        ResponseEntity<String> noTokenResponse = restTemplate.getForEntity("/api/notifications/my", String.class);
        assertThat(noTokenResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        HttpHeaders authHeaders = jsonHeaders();
        authHeaders.setBearerAuth(createToken(1001L, "notification_user"));

        // 3. 创建一条当前用户可见的通知。
        Map<String, Object> createRequest = Map.of(
                "receiverUserId", 1001L,
                "title", "任务提醒",
                "content", "你有一个新的学习任务",
                "type", "TASK",
                "bizType", "TASK",
                "bizId", 9001L
        );
        ResponseEntity<String> createResponse = restTemplate.exchange(
                "/api/notifications",
                HttpMethod.POST,
                new HttpEntity<>(createRequest, authHeaders),
                String.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode createdJson = readJson(createResponse);
        long notificationId = createdJson.path("data").path("id").asLong();
        assertThat(createdJson.path("data").path("readStatus").asInt()).isEqualTo(0);

        // 4. 当前用户可以分页查看自己的通知，并看到刚创建的记录。
        ResponseEntity<String> pageResponse = restTemplate.exchange(
                "/api/notifications/my?current=1&size=10",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders),
                String.class
        );
        assertThat(pageResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readJson(pageResponse).path("data").path("total").asLong()).isGreaterThanOrEqualTo(1);

        // 5. 未读数量随通知创建和已读标记变化。
        ResponseEntity<String> unreadResponse = restTemplate.exchange(
                "/api/notifications/my/unread-count",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders),
                String.class
        );
        assertThat(readJson(unreadResponse).path("data").path("count").asLong()).isEqualTo(1);

        ResponseEntity<String> readResponse = restTemplate.exchange(
                "/api/notifications/" + notificationId + "/read",
                HttpMethod.PUT,
                new HttpEntity<>(authHeaders),
                String.class
        );
        assertThat(readResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readJson(readResponse).path("data").path("readStatus").asInt()).isEqualTo(1);

        ResponseEntity<String> readAllResponse = restTemplate.exchange(
                "/api/notifications/read-all",
                HttpMethod.PUT,
                new HttpEntity<>(authHeaders),
                String.class
        );
        assertThat(readAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readJson(readAllResponse).path("data").path("count").asLong()).isEqualTo(0);
    }

    /**
     * 创建 JSON 请求头，后续再追加 Authorization。
     */
    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * 按 java-demo-app 相同规则生成测试 JWT，模拟用户服务签发 token。
     */
    private String createToken(Long userId, String username) {
        SecretKey signingKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(7200)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 将 HTTP 响应体解析为 JsonNode，便于断言统一响应结构中的嵌套字段。
     */
    private JsonNode readJson(ResponseEntity<String> response) throws Exception {
        return objectMapper.readTree(response.getBody());
    }
}
