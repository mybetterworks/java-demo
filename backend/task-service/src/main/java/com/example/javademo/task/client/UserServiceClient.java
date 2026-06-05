package com.example.javademo.task.client;

import com.example.javademo.task.client.feign.UserFeignClient;
import com.example.javademo.task.common.ApiResponse;
import com.example.javademo.task.common.BusinessException;
import com.example.javademo.task.config.ServiceClientProperties;
import com.example.javademo.task.security.AuthUser;
import feign.FeignException;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * 用户服务客户端。
 *
 * <p>v0.6.1 开始内部实现从 RestTemplate 切换为 OpenFeign。对 TaskService 而言，这里仍然保留一个
 * 语义化包装层，用来集中处理日志、requestId、Authorization 透传和异常映射，避免把这些样板逻辑散落
 * 到业务服务中。</p>
 */
@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    private final UserFeignClient userFeignClient;
    private final ServiceClientProperties properties;

    public UserServiceClient(UserFeignClient userFeignClient, ServiceClientProperties properties) {
        this.userFeignClient = userFeignClient;
        this.properties = properties;
    }

    /**
     * 校验负责人的用户确实存在。
     *
     * @param userId 待校验用户 ID
     * @param currentUser 当前登录用户，用于透传 JWT
     * @return 用户服务返回的最小用户信息
     */
    public UserProfileResponse requireUser(Long userId, AuthUser currentUser) {
        String targetServiceName = properties.getUserServiceName();
        String authorization = buildAuthorizationHeader(currentUser);
        String requestId = currentRequestId();
        long startTime = System.currentTimeMillis();

        try {
            /*
             * 这里显式透传 Authorization 和 requestId，而不是把任务服务自己的认证逻辑复制到下游。
             * 这样 java-demo-app 仍然可以沿用“当前登录用户”语义做鉴权和审计。
             */
            log.info("Calling user service via OpenFeign to validate assignee, assigneeUserId={}, operatorUserId={}, targetService={}",
                    userId, currentUser.getId(), sanitizeTarget(targetServiceName));
            ApiResponse<UserProfileResponse> body = userFeignClient.getUser(userId, authorization, requestId);
            if (body == null || body.getCode() != 0 || body.getData() == null || body.getData().getId() == null) {
                throw BusinessException.downstream("User service returned invalid response");
            }
            log.info("User service validation succeeded, assigneeUserId={}, durationMs={}, targetService={}",
                    userId, System.currentTimeMillis() - startTime, sanitizeTarget(targetServiceName));
            return body.getData();
        } catch (RetryableException exception) {
            /*
             * RetryableException 主要对应连接失败、超时或下游实例暂不可达。这里统一转成 502，
             * 让上层明确知道失败来自下游服务，而不是任务参数本身非法。
             */
            log.warn("User service call failed, assigneeUserId={}, reason={}, targetService={}",
                    userId, exception.getClass().getSimpleName(), sanitizeTarget(targetServiceName));
            throw BusinessException.downstream("User service is unavailable");
        } catch (FeignException exception) {
            // 用户不存在属于可预期业务失败，应当转换为 400，而不是统一吞成 502。
            if (exception.status() == 404) {
                throw BusinessException.badRequest("Assignee user does not exist");
            }
            if (exception.status() == 401) {
                throw BusinessException.unauthorized("User service rejected current token");
            }
            log.warn("User service validation failed, assigneeUserId={}, status={}, targetService={}",
                    userId, exception.status(), sanitizeTarget(targetServiceName));
            throw BusinessException.downstream("User service is unavailable");
        }
    }

    /**
     * 读取当前 requestId，用于把 task-service 和 java-demo-app 的日志串起来。
     */
    private String currentRequestId() {
        String requestId = MDC.get("requestId");
        return requestId == null || requestId.isBlank() ? null : requestId;
    }

    /**
     * 把当前用户 token 重新拼成标准 Bearer 头。
     *
     * <p>日志中绝不打印这个头，但下游服务仍需要它来沿用现有 JWT 鉴权链路。</p>
     */
    private String buildAuthorizationHeader(AuthUser currentUser) {
        if (currentUser.getAccessToken() == null || currentUser.getAccessToken().isBlank()) {
            throw BusinessException.unauthorized("Current user token is missing");
        }
        return "Bearer " + currentUser.getAccessToken();
    }

    /**
     * 对目标服务名或调试地址做兜底脱敏。
     *
     * <p>当前 v0.6.1 主路径使用的是纯服务名；这里仍保留参数脱敏，兼顾未来临时排障时可能引入的调试地址。</p>
     */
    private String sanitizeTarget(String value) {
        return value
                .replaceAll("(?i)(password=)[^&;]+", "$1****")
                .replaceAll("(?i)(pwd=)[^&;]+", "$1****");
    }
}
