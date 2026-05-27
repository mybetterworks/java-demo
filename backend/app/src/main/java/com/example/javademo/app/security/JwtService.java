package com.example.javademo.app.security;

import com.example.javademo.app.common.BusinessException;
import com.example.javademo.app.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT 创建和解析服务。
 *
 * <p>该类封装 JJWT 库的使用细节，让业务层只关心“根据用户生成 token”和“根据 token 解析用户”。
 * 当前使用 HS256 对称签名算法，因此签发和校验使用同一个密钥；后续如果升级为非对称密钥，
 * 也可以集中在这里调整。</p>
 */
@Service
public class JwtService {

    /** 自定义声明字段，用于在 token 中保存用户名，subject 则保存用户 ID。 */
    private static final String USERNAME_CLAIM = "username";

    /** JWT 日志只记录签发用户、过期时间和失败类型，绝不打印 token 或签名密钥。 */
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    /**
     * 初始化 JWT 服务并校验密钥长度。
     *
     * <p>HS256 要求密钥至少 256 bit，也就是 32 字节。启动阶段提前失败比运行时签发 token 再失败更容易排查。</p>
     */
    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        if (jwtProperties.getSecret() == null || jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT secret must contain at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 根据用户信息创建 JWT。
     *
     * @param userId 用户 ID，写入 JWT subject
     * @param username 用户名，写入自定义 claim
     * @return 已签名的 JWT 字符串
     */
    public String createToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtProperties.getExpirationSeconds());

        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(USERNAME_CLAIM, username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
        // DEBUG 日志只记录 token 元数据，不能记录 token 字符串本身。
        log.debug("JWT created, userId={}, username={}, expiresAt={}", userId, username, expiresAt);
        return token;
    }

    /**
     * 解析并校验 JWT。
     *
     * <p>这里会同时完成签名校验和过期时间校验。任何解析失败都会统一转换成 401 业务异常，
     * 调用方无需关心 JJWT 的底层异常类型。</p>
     *
     * @param token 去掉 Bearer 前缀后的 JWT 字符串
     * @return token 中携带的认证用户信息
     */
    public AuthUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long userId = Long.valueOf(claims.getSubject());
            String username = claims.get(USERNAME_CLAIM, String.class);
            log.debug("JWT parsed, userId={}, username={}", userId, username);
            return new AuthUser(userId, username);
        } catch (JwtException | IllegalArgumentException exception) {
            // 解析失败时只记录异常类型，避免把可能包含 token 内容的信息写入日志。
            log.warn("JWT parse failed, reason={}", exception.getClass().getSimpleName());
            throw BusinessException.unauthorized("Invalid or expired token");
        }
    }

    /**
     * 返回 token 有效期，登录接口会把这个值返回给前端，便于前端做过期提示或刷新策略。
     */
    public long getExpirationSeconds() {
        return jwtProperties.getExpirationSeconds();
    }
}
