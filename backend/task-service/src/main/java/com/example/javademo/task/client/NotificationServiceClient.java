package com.example.javademo.task.client;

import com.example.javademo.task.common.ApiResponse;
import com.example.javademo.task.common.BusinessException;
import com.example.javademo.task.config.ServiceClientProperties;
import com.example.javademo.task.entity.TaskItem;
import com.example.javademo.task.security.AuthUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * 通知服务 REST 客户端。
 *
 * <p>任务分配和状态变化后，任务服务会同步调用 notification-service 创建站内信。
 * 这条同步链路会在后续 v1.0 MQ milestone 中演进为异步事件，当前先保持最小可理解实现。</p>
 */
@Component
public class NotificationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceClient.class);

    /** 服务间调用透传 requestId 的请求头名称。 */
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final RestTemplate restTemplate;
    private final ServiceClientProperties properties;

    public NotificationServiceClient(RestTemplate restTemplate, ServiceClientProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    /**
     * 创建任务相关通知。
     *
     * @param receiverUserId 接收通知的用户 ID
     * @param title          通知标题
     * @param content        通知内容
     * @param task           关联任务
     * @param currentUser    当前登录用户，用于转发 JWT
     */
    public void createTaskNotification(Long receiverUserId, String title, String content, TaskItem task, AuthUser currentUser) {
        String url = trimTrailingSlash(properties.getNotificationServiceUrl()) + "/api/notifications";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getAccessToken());
        attachRequestId(headers);

        CreateNotificationRequest request = new CreateNotificationRequest(
                receiverUserId,
                title,
                content,
                "TASK",
                "TASK",
                task.getId()
        );
        long startTime = System.currentTimeMillis();

        try {
            // 通知内容可能包含用户输入，服务间调用日志只记录任务 ID 和接收人 ID。
            log.info("Calling notification service, receiverUserId={}, taskId={}, operatorUserId={}",
                    receiverUserId, task.getId(), currentUser.getId());
            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(request, headers),
                    ApiResponse.class
            );
            ApiResponse body = response.getBody();
            if (body == null || body.getCode() != 0) {
                throw BusinessException.downstream("Notification service returned invalid response");
            }
            log.info("Notification service call succeeded, receiverUserId={}, taskId={}, status={}, durationMs={}",
                    receiverUserId, task.getId(), response.getStatusCode().value(), System.currentTimeMillis() - startTime);
        } catch (HttpStatusCodeException exception) {
            log.warn("Notification service rejected request, taskId={}, receiverUserId={}, status={}",
                    task.getId(), receiverUserId, exception.getStatusCode().value());
            throw BusinessException.downstream("Notification service is unavailable");
        } catch (RestClientException exception) {
            log.warn("Notification service call failed, taskId={}, receiverUserId={}, reason={}",
                    task.getId(), receiverUserId, exception.getClass().getSimpleName());
            throw BusinessException.downstream("Notification service is unavailable");
        }
    }

    /**
     * 把当前请求的 requestId 透传给 notification-service。
     *
     * <p>这不是完整链路追踪，但足够让 v0.5.2 的本地文件日志把任务创建和通知创建串起来。</p>
     */
    private void attachRequestId(HttpHeaders headers) {
        String requestId = MDC.get("requestId");
        if (requestId != null && !requestId.isBlank()) {
            headers.set(REQUEST_ID_HEADER, requestId);
        }
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:8094";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
