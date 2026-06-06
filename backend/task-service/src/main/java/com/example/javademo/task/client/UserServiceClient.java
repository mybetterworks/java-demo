package com.example.javademo.task.client;

import com.example.javademo.rpc.DubboAttachmentConstants;
import com.example.javademo.rpc.user.UserRpcService;
import com.example.javademo.rpc.user.UserSummaryRpcResponse;
import com.example.javademo.task.common.BusinessException;
import com.example.javademo.task.config.ServiceClientProperties;
import com.example.javademo.task.security.AuthUser;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 用户服务客户端。
 *
 * <p>v0.6.2 开始，这个包装层把负责人用户校验从 OpenFeign 切换到了 Dubbo。
 * 业务层依然只需要调用语义化的 `requireUser`，
 * 而 requestId 透传、RPC 调用日志、异常转换和 DTO 映射都继续收敛在这里。</p>
 */
@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    private final UserRpcService userRpcService;
    private final ServiceClientProperties properties;

    public UserServiceClient(@Qualifier("userRpcServiceBridge") UserRpcService userRpcService,
                             ServiceClientProperties properties) {
        this.userRpcService = userRpcService;
        this.properties = properties;
    }

    /**
     * 校验负责人用户确实存在。
     *
     * @param userId 待校验用户 ID
     * @param currentUser 当前登录用户，用于记录操作人并串联日志
     * @return 任务服务内部继续沿用的最小用户摘要
     */
    public UserProfileResponse requireUser(Long userId, AuthUser currentUser) {
        String targetServiceName = properties.getUserServiceName();
        String requestId = currentRequestId();
        long startTime = System.currentTimeMillis();

        try {
            /*
             * Dubbo provider 不会自动继承当前 HTTP 线程里的 MDC，
             * 因此这里显式透传 requestId，方便 java-demo-app 在 provider 侧恢复日志上下文。
             */
            if (requestId != null) {
                RpcContext.getClientAttachment().setAttachment(DubboAttachmentConstants.REQUEST_ID, requestId);
            }

            log.info("Calling user service via Dubbo to validate assignee, assigneeUserId={}, operatorUserId={}, targetService={}",
                    userId, resolveOperatorUserId(currentUser), sanitizeTarget(targetServiceName));
            UserSummaryRpcResponse rpcResponse = userRpcService.getUserSummary(userId);

            /*
             * provider 用 null 表达“用户不存在或已逻辑删除”，
             * 这样 task-service 可以继续保留自己原有的 400 业务语义，而不会把这类场景误判成 502。
             */
            if (rpcResponse == null) {
                throw BusinessException.badRequest("Assignee user does not exist");
            }
            if (rpcResponse.getId() == null) {
                throw BusinessException.downstream("User service returned invalid RPC response");
            }

            UserProfileResponse response = toUserProfileResponse(rpcResponse);
            log.info("User service validation succeeded, assigneeUserId={}, durationMs={}, targetService={}",
                    userId, System.currentTimeMillis() - startTime, sanitizeTarget(targetServiceName));
            return response;
        } catch (RpcException exception) {
            log.warn("User service Dubbo call failed, assigneeUserId={}, reason={}, targetService={}",
                    userId, exception.getClass().getSimpleName(), sanitizeTarget(targetServiceName));
            throw BusinessException.downstream("User service is unavailable");
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("User service Dubbo call returned unexpected exception, assigneeUserId={}, reason={}, targetService={}",
                    userId, exception.getClass().getSimpleName(), sanitizeTarget(targetServiceName));
            throw BusinessException.downstream("User service is unavailable");
        } finally {
            RpcContext.removeClientAttachment();
        }
    }

    /**
     * 读取当前 requestId，供 Dubbo 附件透传使用。
     */
    private String currentRequestId() {
        String requestId = MDC.get("requestId");
        return requestId == null || requestId.isBlank() ? null : requestId;
    }

    /**
     * 把 Dubbo DTO 转换成任务服务内部继续沿用的用户摘要对象。
     */
    private UserProfileResponse toUserProfileResponse(UserSummaryRpcResponse rpcResponse) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(rpcResponse.getId());
        response.setUsername(rpcResponse.getUsername());
        response.setStatus(rpcResponse.getStatus());
        return response;
    }

    /**
     * 提取日志里真正需要的操作人用户 ID。
     */
    private Long resolveOperatorUserId(AuthUser currentUser) {
        return currentUser == null ? null : currentUser.getId();
    }

    /**
     * 对目标服务名或调试地址做兜底脱敏。
     */
    private String sanitizeTarget(String value) {
        return value
                .replaceAll("(?i)(password=)[^&;]+", "$1****")
                .replaceAll("(?i)(pwd=)[^&;]+", "$1****");
    }
}
