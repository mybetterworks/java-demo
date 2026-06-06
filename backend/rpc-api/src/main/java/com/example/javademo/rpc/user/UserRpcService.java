package com.example.javademo.rpc.user;

/**
 * 用户领域的内部 RPC 契约。
 *
 * <p>这个接口只用于后端内部的 Dubbo 同步调用，不对前端、Gateway 或 Swagger 暴露。
 * v0.6.2 选择把“任务创建/更新时的负责人用户校验”迁移到 Dubbo，
 * 因此契约只保留 task-service 当前真正需要的最小用户摘要能力。</p>
 */
public interface UserRpcService {

    /**
     * 根据用户 ID 查询可用于任务负责人校验的最小用户摘要。
     *
     * <p>如果目标用户不存在，或者已经被逻辑删除，则返回 {@code null}。
     * 这样 consumer 可以把“用户不存在”明确映射成自己的 400 业务错误，
     * 同时把“RPC 调用失败”与“业务用户缺失”区分开来。</p>
     *
     * @param userId 待校验的用户 ID
     * @return 用户摘要；不存在时返回 {@code null}
     */
    UserSummaryRpcResponse getUserSummary(Long userId);
}
