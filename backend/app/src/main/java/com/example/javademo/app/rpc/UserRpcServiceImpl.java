package com.example.javademo.app.rpc;

import com.example.javademo.app.common.BusinessException;
import com.example.javademo.app.dto.UserProfileResponse;
import com.example.javademo.app.service.UserManagementService;
import com.example.javademo.rpc.DubboAttachmentConstants;
import com.example.javademo.rpc.user.UserRpcService;
import com.example.javademo.rpc.user.UserSummaryRpcResponse;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * 用户 Dubbo RPC provider。
 *
 * <p>v0.6.2 只把 task-service 的负责人校验链路迁移到 Dubbo，
 * 因此 provider 也保持最小职责：复用已有用户管理领域逻辑，
 * 暴露一个最小的用户摘要查询接口，而不是复制一套新的用户校验规则。</p>
 */
@DubboService
public class UserRpcServiceImpl implements UserRpcService {

    private static final Logger log = LoggerFactory.getLogger(UserRpcServiceImpl.class);

    private final UserManagementService userManagementService;

    public UserRpcServiceImpl(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @Override
    public UserSummaryRpcResponse getUserSummary(Long userId) {
        long startTime = System.currentTimeMillis();
        String requestId = currentRpcRequestId();
        String previousRequestId = MDC.get("requestId");
        boolean requestIdInjectedByRpc = false;

        /*
         * Dubbo provider 默认没有经过 HTTP 过滤器，因此这里把 consumer 透传过来的 requestId 写回 MDC，
         * 让下游领域服务和异常日志继续共用同一个 requestId。
         */
        if ((previousRequestId == null || previousRequestId.isBlank()) && requestId != null) {
            MDC.put("requestId", requestId);
            requestIdInjectedByRpc = true;
        }

        try {
            log.info("Received Dubbo user validation request, userId={}, requestIdPresent={}", userId, requestId != null);
            UserProfileResponse user = userManagementService.getUser(userId);
            UserSummaryRpcResponse response = new UserSummaryRpcResponse(user.getId(), user.getUsername(), user.getStatus());
            log.info("Dubbo user validation succeeded, userId={}, durationMs={}", userId, System.currentTimeMillis() - startTime);
            return response;
        } catch (BusinessException exception) {
            /*
             * “用户不存在”是这条链路上可预期的业务结果，
             * provider 不把本地业务异常类型直接跨 RPC 传给 consumer，而是统一收敛为 null。
             */
            if (exception.getCode() == 404) {
                log.info("Dubbo user validation found no available user, userId={}, durationMs={}",
                        userId, System.currentTimeMillis() - startTime);
                return null;
            }
            log.warn("Dubbo user validation rejected request, userId={}, code={}, message={}",
                    userId, exception.getCode(), exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            log.error("Dubbo user validation failed unexpectedly, userId={}", userId, exception);
            throw exception;
        } finally {
            if (requestIdInjectedByRpc) {
                MDC.remove("requestId");
            }
        }
    }

    /**
     * 读取 consumer 透传的 requestId。
     */
    private String currentRpcRequestId() {
        String requestId = RpcContext.getServerAttachment().getAttachment(DubboAttachmentConstants.REQUEST_ID);
        return requestId == null || requestId.isBlank() ? null : requestId;
    }
}
