package com.example.javademo.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 网关 JWT 配置。
 *
 * <p>这里使用和 backend/app 完全相同的配置前缀与默认密钥，保证后端签发的 token
 * 可以被 Gateway 正确校验。真实系统中默认密钥必须通过环境变量覆盖，本项目当前保留默认值
 * 只是为了本地学习和从 0 到 1 的可运行体验。</p>
 */
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** JWT 签名密钥，HS256 至少需要 32 字节，启动时会在 JwtVerifier 中校验。 */
    private String secret;

    /** JWT 有效期，网关只校验 token 自带过期时间，该值主要用于文档和后续刷新策略对齐。 */
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
