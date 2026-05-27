package com.example.javademo.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * task-service 集成测试。
 *
 * <p>测试会启动真实任务服务、H2 数据库和 JWT 拦截器；用户服务与通知服务通过 MockRestServiceServer
 * 模拟。这样既验证了任务服务自身闭环，也验证了 v0.5.1 最关键的同步 REST 服务间调用。</p>
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskIntegrationTest {

    private static final String TEST_SECRET = "java-demo-test-secret-change-me-32chars";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate downstreamRestTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        /*
         * 同一个任务用例会按“创建任务 -> 改状态 -> 改负责人”的顺序多次调用下游服务。
         * MockRestServiceServer 不允许在真实请求已经发生后再追加 expectation，因此这里使用忽略顺序的
         * server，并在发起第一个业务请求前一次性注册全部下游调用预期。
         */
        mockServer = MockRestServiceServer.bindTo(downstreamRestTemplate)
                .ignoreExpectOrder(true)
                .build();
    }

    @Test
    void shouldCreateUpdateAndDeleteTaskWithDownstreamCalls() throws Exception {
        String token = createToken(1001L, "task_user");

        // 1. 健康检查公开访问；任务业务接口必须携带 JWT。
        ResponseEntity<String> healthResponse = restTemplate.getForEntity("/api/tasks/health", String.class);
        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readJson(healthResponse).path("data").path("service").asText()).isEqualTo("task-service");

        ResponseEntity<String> noTokenResponse = restTemplate.getForEntity("/api/tasks", String.class);
        assertThat(noTokenResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        HttpHeaders authHeaders = jsonHeaders();
        authHeaders.setBearerAuth(token);

        expectUserExists(1001L, token);
        expectUserExists(1002L, token);
        expectNotifications(token, 3);

        Map<String, Object> createRequest = Map.of(
                "title", "完成 v0.5.1 任务服务",
                "description", "验证任务创建、通知和分页查询",
                "assigneeUserId", 1001L,
                "priority", "HIGH",
                "dueTime", LocalDateTime.now().plusDays(1).toString()
        );
        ResponseEntity<String> createResponse = restTemplate.exchange(
                "/api/tasks",
                HttpMethod.POST,
                new HttpEntity<>(createRequest, authHeaders),
                String.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode createdJson = readJson(createResponse);
        long taskId = createdJson.path("data").path("id").asLong();
        assertThat(createdJson.path("data").path("status").asText()).isEqualTo("TODO");
        assertThat(createdJson.path("data").path("priority").asText()).isEqualTo("HIGH");

        // 2. 我的任务和详情查询都能读到刚创建的任务。
        ResponseEntity<String> myTasksResponse = restTemplate.exchange(
                "/api/tasks/my?current=1&size=10",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders),
                String.class
        );
        assertThat(myTasksResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readJson(myTasksResponse).path("data").path("total").asLong()).isEqualTo(1);

        ResponseEntity<String> detailResponse = restTemplate.exchange(
                "/api/tasks/" + taskId,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders),
                String.class
        );
        assertThat(detailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readJson(detailResponse).path("data").path("id").asLong()).isEqualTo(taskId);

        // 3. 状态变化会更新任务并再次创建通知。
        ResponseEntity<String> statusResponse = restTemplate.exchange(
                "/api/tasks/" + taskId + "/status",
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("status", "IN_PROGRESS"), authHeaders),
                String.class
        );
        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readJson(statusResponse).path("data").path("status").asText()).isEqualTo("IN_PROGRESS");

        // 4. 修改负责人会重新校验用户并通知新的负责人。
        ResponseEntity<String> updateResponse = restTemplate.exchange(
                "/api/tasks/" + taskId,
                HttpMethod.PUT,
                new HttpEntity<>(Map.of(
                        "title", "完成 v0.5.1 任务服务联调",
                        "assigneeUserId", 1002L,
                        "priority", "MEDIUM"
                ), authHeaders),
                String.class
        );
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode updatedJson = readJson(updateResponse);
        assertThat(updatedJson.path("data").path("assigneeUserId").asLong()).isEqualTo(1002L);

        // 5. 逻辑删除后详情不可查，保留数据但不再出现在默认查询中。
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                "/api/tasks/" + taskId,
                HttpMethod.DELETE,
                new HttpEntity<>(authHeaders),
                String.class
        );
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> deletedDetailResponse = restTemplate.exchange(
                "/api/tasks/" + taskId,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders),
                String.class
        );
        assertThat(deletedDetailResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        mockServer.verify();
    }

    private void expectUserExists(Long userId, String token) {
        mockServer.expect(once(), requestTo("http://java-demo-app/api/users/" + userId))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andRespond(withSuccess("""
                        {"code":0,"message":"success","data":{"id":%d,"username":"user_%d","status":1}}
                        """.formatted(userId, userId), MediaType.APPLICATION_JSON));
    }

    private void expectNotifications(String token, int count) {
        mockServer.expect(times(count), requestTo("http://notification-service/api/notifications"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andRespond(withSuccess("""
                        {"code":0,"message":"created","data":{"id":501}}
                        """, MediaType.APPLICATION_JSON));
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

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

    private JsonNode readJson(ResponseEntity<String> response) throws Exception {
        return objectMapper.readTree(response.getBody());
    }
}
