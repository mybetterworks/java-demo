package com.example.javademo.gateway.security;

import com.example.javademo.gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * 网关层 JWT 验签组件。
 *
 * <p>backend/app 负责签发 token，Gateway 负责在转发前做第一道校验。
 * 两边使用同一个 app.jwt.secret，因此网关可以独立完成签名校验和过期时间校验。
 * 当前没有引入远程会话或 Redis 黑名单，所以“退出登录后立即吊销 token”暂不属于 v0.5 范围。</p>
 */
@Component
public class JwtVerifier {

    /** 与后端 JwtService 保持一致：subject 保存用户 ID，username claim 保存用户名。 */
    private static final String USERNAME_CLAIM = "username";

    private final SecretKey signingKey;

    /**
     * 初始化验签密钥。
     *
     * <p>HS256 至少要求 32 字节密钥。网关启动时提前检查，避免运行到第一笔请求才暴露配置错误。</p>
     */
    public JwtVerifier(JwtProperties jwtProperties) {
        if (jwtProperties.getSecret() == null || jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT secret must contain at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 校验 token 并解析最小用户信息。
     *
     * @param token 去掉 Bearer 前缀后的 JWT 字符串
     * @return 网关识别出的用户 ID 和用户名
     */
    public GatewayAuthUser verify(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long userId = Long.valueOf(claims.getSubject());
            String username = claims.get(USERNAME_CLAIM, String.class);
            return new GatewayAuthUser(userId, username);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new JwtAuthenticationException("Invalid or expired token", exception);
        }
    }
}
