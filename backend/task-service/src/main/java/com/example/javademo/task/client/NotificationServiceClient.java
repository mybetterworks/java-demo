package com.example.javademo.task.client;

import com.example.javademo.task.client.feign.NotificationFeignClient;
import com.example.javademo.task.common.ApiResponse;
import com.example.javademo.task.common.BusinessException;
import com.example.javademo.task.config.ServiceClientProperties;
import com.example.javademo.task.entity.TaskItem;
import com.example.javademo.task.security.AuthUser;
import feign.FeignException;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * 通知服务客户端。
 *
 * <p>任务分配和状态变化后，任务服务会同步调用 notification-service 创建站内信。v0.6.1 开始
 * 这条链路的底层实现切换为 OpenFeign，但业务层仍通过这个包装类统一处理日志、请求头透传与错误映射。</p>
 */
@Component
public class NotificationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceClient.class);

    private final NotificationFeignClient notificationFeignClient;
    private final ServiceClientProperties properties;

    public NotificationServiceClient(NotificationFeignClient notificationFeignClient, ServiceClientProperties properties) {
        this.notificationFeignClient = notificationFeignClient;
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
        String targetServiceName = properties.getNotificationServiceName();
        String authorization = buildAuthorizationHeader(currentUser);
        String requestId = currentRequestId();

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
            // 通知内容可能包含用户输入，因此日志只记录任务 ID、接收人和目标服务名，不打印正文和 token。
            log.info("Calling notification service via OpenFeign, receiverUserId={}, taskId={}, operatorUserId={}, targetService={}",
                    receiverUserId, task.getId(), currentUser.getId(), sanitizeTarget(targetServiceName));
            ApiResponse<Object> body = notificationFeignClient.createNotification(request, authorization, requestId);
            if (body == null || body.getCode() != 0) {
                throw BusinessException.downstream("Notification service returned invalid response");
            }
            log.info("Notification service call succeeded, receiverUserId={}, taskId={}, durationMs={}, targetService={}",
                    receiverUserId, task.getId(), System.currentTimeMillis() - startTime, sanitizeTarget(targetServiceName));
        } catch (RetryableException exception) {
            /*
             * 通知服务超时或连接失败时，当前版本仍按同步强依赖处理，让事务回滚并把失败明确暴露出来，
             * 方便后续与 v1.0 的 MQ 异步方案做对比。
             */
            log.warn("Notification service call failed, taskId={}, receiverUserId={}, reason={}, targetService={}",
                    task.getId(), receiverUserId, exception.getClass().getSimpleName(), sanitizeTarget(targetServiceName));
            throw BusinessException.downstream("Notification service is unavailable");
        } catch (FeignException exception) {
            log.warn("Notification service rejected request, taskId={}, receiverUserId={}, status={}, targetService={}",
                    task.getId(), receiverUserId, exception.status(), sanitizeTarget(targetServiceName));
            throw BusinessException.downstream("Notification service is unavailable");
        }
    }

    /**
     * 读取当前 requestId，方便通知服务日志和任务服务日志串联。
     */
    private String currentRequestId() {
        String requestId = MDC.get("requestId");
        return requestId == null || requestId.isBlank() ? null : requestId;
    }

    /**
     * 把当前用户 token 重新拼成标准 Bearer 头。
     */
    private String buildAuthorizationHeader(AuthUser currentUser) {
        if (currentUser.getAccessToken() == null || currentUser.getAccessToken().isBlank()) {
            throw BusinessException.unauthorized("Current user token is missing");
        }
        return "Bearer " + currentUser.getAccessToken();
    }

    /**
     * 对下游目标服务名或临时调试地址做兜底脱敏，避免把凭据类参数打到日志里。
     */
    private String sanitizeTarget(String value) {
        return value
                .replaceAll("(?i)(password=)[^&;]+", "$1****")
                .replaceAll("(?i)(pwd=)[^&;]+", "$1****");
    }
}
