package com.example.javademo.task;

import com.example.javademo.task.client.CreateNotificationRequest;
import com.example.javademo.task.client.UserProfileResponse;
import com.example.javademo.task.client.feign.NotificationFeignClient;
import com.example.javademo.task.client.feign.UserFeignClient;
import com.example.javademo.task.common.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * task-service 集成测试。
 *
 * <p>测试会启动真实任务服务、H2 数据库和 JWT 拦截器；下游用户服务与通知服务则通过 MockBean 替换
 * OpenFeign 客户端。这样既能验证任务服务的真实 HTTP 入口，也能验证 v0.6.1 的 Feign 调用、
 * requestId 透传和错误映射是否仍然符合原有业务语义。</p>
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskIntegrationTest {

    private static final String TEST_SECRET = "java-demo-test-secret-change-me-32chars";
    private static final String REQUEST_ID = "task-test-request-id";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserFeignClient userFeignClient;

    @MockBean
    private NotificationFeignClient notificationFeignClient;

    @BeforeEach
    void setUp() {
        reset(userFeignClient, notificationFeignClient);
    }

    @Test
    void shouldCreateUpdateAndDeleteTaskWithFeignCalls() throws Exception {
        String token = createToken(1001L, "task_user");

        // 1. 健康检查公开访问；同时确认服务调用模式已经切换到 v0.6.1 的 OpenFeign。
        ResponseEntity<String> healthResponse = restTemplate.getForEntity("/api/tasks/health", String.class);
        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readJson(healthResponse).path("data").path("service").asText()).isEqualTo("task-service");
        assertThat(readJson(healthResponse).path("data").path("serviceCallMode").asText()).isEqualTo("openfeign");

        ResponseEntity<String> noTokenResponse = restTemplate.getForEntity("/api/tasks", String.class);
        assertThat(noTokenResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        HttpHeaders authHeaders = jsonHeaders(token, REQUEST_ID);

        /*
         * 两次用户校验分别对应“创建任务时校验负责人”和“更新负责人时重新校验负责人”。
         * 通知创建会在创建任务、更新状态和变更负责人这三个关键事件各发生一次。
         */
        mockUserExists(1001L);
        mockUserExists(1002L);
        mockNotificationCreated();

        Map<String, Object> createRequest = Map.of(
                "title", "完成 v0.6.1 OpenFeign 联调",
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
                        "title", "完成 v0.6.1 任务服务联调",
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

        // 验证 Feign 客户端确实拿到了 Bearer token 和 requestId，而不是退回到无上下文的裸调用。
        verify(userFeignClient).getUser(1001L, "Bearer " + token, REQUEST_ID);
        verify(userFeignClient).getUser(1002L, "Bearer " + token, REQUEST_ID);
        verify(notificationFeignClient, times(3))
                .createNotification(any(CreateNotificationRequest.class), eq("Bearer " + token), eq(REQUEST_ID));
    }

    @Test
    void shouldReturnBadRequestWhenAssigneeUserDoesNotExist() throws Exception {
        String token = createToken(1001L, "task_user");
        HttpHeaders authHeaders = jsonHeaders(token, "task-test-user-not-found");

        // 用户服务返回 404 时，应当转成任务服务自己的 400 业务错误，而不是暴露底层 Feign 细节。
        when(userFeignClient.getUser(eq(9999L), anyString(), anyString()))
                .thenThrow(buildFeignStatusException(404, HttpMethod.GET, "http://java-demo-app/api/users/9999"));

        ResponseEntity<String> createResponse = restTemplate.exchange(
                "/api/tasks",
                HttpMethod.POST,
                new HttpEntity<>(Map.of(
                        "title", "负责人不存在",
                        "assigneeUserId", 9999L
                ), authHeaders),
                String.class
        );

        JsonNode responseJson = readJson(createResponse);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseJson.path("code").asInt()).isEqualTo(400);
        assertThat(responseJson.path("message").asText()).isEqualTo("Assignee user does not exist");
        verify(notificationFeignClient, never()).createNotification(any(), anyString(), nullable(String.class));
    }

    @Test
    void shouldReturnBadGatewayAndRollbackTaskWhenNotificationServiceFails() throws Exception {
        String token = createToken(1001L, "task_user");
        String requestId = "task-test-notification-failed";
        HttpHeaders authHeaders = jsonHeaders(token, requestId);

        mockUserExists(1001L);
        when(notificationFeignClient.createNotification(any(CreateNotificationRequest.class), eq("Bearer " + token), eq(requestId)))
                .thenThrow(buildFeignStatusException(503, HttpMethod.POST, "http://notification-service/api/notifications"));

        ResponseEntity<String> createResponse = restTemplate.exchange(
                "/api/tasks",
                HttpMethod.POST,
                new HttpEntity<>(Map.of(
                        "title", "通知失败应回滚",
                        "assigneeUserId", 1001L
                ), authHeaders),
                String.class
        );

        JsonNode responseJson = readJson(createResponse);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(responseJson.path("code").asInt()).isEqualTo(502);
        assertThat(responseJson.path("message").asText()).isEqualTo("Notification service is unavailable");

        /*
         * 当前 v0.6.1 仍保留同步强依赖链路，因此通知失败会回滚任务写入。
         * 这里顺手把事务语义也一起锁住，避免后续改造时悄悄破坏。
         */
        ResponseEntity<String> myTasksResponse = restTemplate.exchange(
                "/api/tasks/my?current=1&size=10",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders),
                String.class
        );
        assertThat(myTasksResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readJson(myTasksResponse).path("data").path("total").asLong()).isEqualTo(0);
    }

    private void mockUserExists(Long userId) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(userId);
        response.setUsername("user_" + userId);
        response.setStatus(1);
        when(userFeignClient.getUser(eq(userId), anyString(), anyString()))
                .thenReturn(ApiResponse.success(response));
    }

    private void mockNotificationCreated() {
        when(notificationFeignClient.createNotification(any(CreateNotificationRequest.class), anyString(), anyString()))
                .thenReturn(ApiResponse.success("created", Map.of("id", 501L)));
    }

    private HttpHeaders jsonHeaders(String token, String requestId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        headers.set("X-Request-Id", requestId);
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

    /**
     * 构造一个最小可用的 Feign HTTP 异常，用来模拟真实下游服务的 4xx / 5xx 响应。
     */
    private FeignException buildFeignStatusException(int status, HttpMethod method, String url) {
        Request request = Request.create(Request.HttpMethod.valueOf(method.name()), url, Map.of(), null, StandardCharsets.UTF_8, new RequestTemplate());
        Response response = Response.builder()
                .status(status)
                .reason("mock-feign-error")
                .request(request)
                .headers(Map.of())
                .body(new byte[0])
                .build();
        return FeignException.errorStatus("mock-feign-client", response);
    }
}
