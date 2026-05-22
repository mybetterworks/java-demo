package com.example.javademo.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置属性。
 *
 * <p>通过 @ConfigurationProperties 绑定 application.yml 中 app.jwt 前缀下的配置。
 * 这样密钥和过期时间可以通过环境变量覆盖，避免把真实生产密钥写死在代码里。</p>
 */
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** JWT 签名密钥，HS256 至少需要 32 字节，JwtService 启动时会校验长度。 */
    private String secret;

    /** JWT 有效期，单位秒；v0.1 默认 7200 秒，也就是 2 小时。 */
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
