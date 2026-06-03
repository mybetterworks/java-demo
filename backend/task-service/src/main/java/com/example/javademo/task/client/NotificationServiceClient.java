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
 * <p>任务分配和状态变化后，任务服务会同步调用 notification-service 创建站内信。v0.6 开始这条链路
 * 默认通过服务发现访问通知服务，但仍保留最小同步 HTTP 方式，把异步化留给后续 MQ milestone。</p>
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
     * @param title 通知标题
     * @param content 通知内容
     * @param task 关联任务
     * @param currentUser 当前登录用户，用于透传 JWT
     */
    public void createTaskNotification(Long receiverUserId, String title, String content, TaskItem task, AuthUser currentUser) {
        String baseUrl = trimTrailingSlash(properties.getNotificationServiceUrl());
        String url = baseUrl + "/api/notifications";
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
            // 通知内容可能包含用户输入，因此日志只记录任务 ID、接收人和目标服务地址。
            log.info("Calling notification service, receiverUserId={}, taskId={}, operatorUserId={}, target={}",
                    receiverUserId, task.getId(), currentUser.getId(), sanitizeTarget(baseUrl));
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
            log.info("Notification service call succeeded, receiverUserId={}, taskId={}, status={}, durationMs={}, target={}",
                    receiverUserId, task.getId(), response.getStatusCode().value(), System.currentTimeMillis() - startTime, sanitizeTarget(baseUrl));
        } catch (HttpStatusCodeException exception) {
            log.warn("Notification service rejected request, taskId={}, receiverUserId={}, status={}, target={}",
                    task.getId(), receiverUserId, exception.getStatusCode().value(), sanitizeTarget(baseUrl));
            throw BusinessException.downstream("Notification service is unavailable");
        } catch (RestClientException exception) {
            log.warn("Notification service call failed, taskId={}, receiverUserId={}, reason={}, target={}",
                    task.getId(), receiverUserId, exception.getClass().getSimpleName(), sanitizeTarget(baseUrl));
            throw BusinessException.downstream("Notification service is unavailable");
        }
    }

    /**
     * 把当前请求的 requestId 透传给 notification-service。
     */
    private void attachRequestId(HttpHeaders headers) {
        String requestId = MDC.get("requestId");
        if (requestId != null && !requestId.isBlank()) {
            headers.set(REQUEST_ID_HEADER, requestId);
        }
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://notification-service";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    /**
     * 对下游目标地址做兜底脱敏，避免后续扩展时把凭据类参数打到日志里。
     */
    private String sanitizeTarget(String value) {
        return value
                .replaceAll("(?i)(password=)[^&;]+", "$1****")
                .replaceAll("(?i)(pwd=)[^&;]+", "$1****");
    }
}
