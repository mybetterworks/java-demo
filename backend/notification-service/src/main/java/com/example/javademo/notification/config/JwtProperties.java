package com.example.javademo.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 通知服务 JWT 配置。
 *
 * <p>通知服务需要独立校验 JWT，因此必须和 java-demo-app、Gateway 使用同一个 app.jwt.secret。
 * 当前默认密钥只用于本地学习环境，真实环境必须通过环境变量覆盖。</p>
 */
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** JWT 签名密钥，HS256 至少需要 32 字节。 */
    private String secret;

    /** JWT 有效期，通知服务解析时主要依赖 token 自带过期时间。 */
    private long expirationSeconds = 7200;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public void setExpirationSeconds(long expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
    }
}
