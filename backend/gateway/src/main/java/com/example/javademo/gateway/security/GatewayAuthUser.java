package com.example.javademo.gateway.security;

/**
 * 网关解析 JWT 后得到的最小认证用户信息。
 *
 * <p>v0.5 阶段后端仍然会再次解析 JWT，所以网关暂时不把该对象作为业务上下文使用。
 * 这里保留 userId 和 username，是为了后续学习“网关向下游传递可信用户头”时有清晰扩展点。</p>
 *
 * @param userId token subject 中的用户 ID
 * @param username token 自定义 claim 中的用户名
 */
public record GatewayAuthUser(Long userId, String username) {
}
