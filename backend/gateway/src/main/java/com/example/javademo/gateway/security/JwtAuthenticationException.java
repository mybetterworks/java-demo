package com.example.javademo.gateway.security;

/**
 * 网关 JWT 校验失败异常。
 *
 * <p>使用独立异常类型可以避免把 JJWT 的底层异常直接暴露到过滤器之外，
 * 过滤器只需要捕获这一类异常并返回统一 401 响应。</p>
 */
public class JwtAuthenticationException extends RuntimeException {

    public JwtAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
