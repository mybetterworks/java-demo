package com.example.javademo.task.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 任务服务 JWT 配置。
 *
 * <p>任务服务不签发 token，只校验 java-demo-app 签发的 token。因此这里的密钥必须和
 * java-demo-app、Gateway、notification-service 保持一致。</p>
 */
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** JWT 签名密钥，HS256 至少需要 32 字节。 */
    private String secret;

    /** JWT 有效期配置在解析时主要用于和其他服务保持配置模型一致。 */
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
