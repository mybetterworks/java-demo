package com.example.javademo.task.security;

import com.example.javademo.task.common.BusinessException;
import com.example.javademo.task.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * 任务服务 JWT 解析服务。
 *
 * <p>Gateway 是第一道校验，但任务服务仍保留自己的验签能力，确保开发者直连 8093 时也不能绕过认证。</p>
 */
@Service
public class JwtService {

    private static final String USERNAME_CLAIM = "username";

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        if (jwtProperties.getSecret() == null || jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT secret must contain at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public AuthUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long userId = Long.valueOf(claims.getSubject());
            String username = claims.get(USERNAME_CLAIM, String.class);
            // DEBUG 只输出解析后的用户摘要，不输出原始 JWT。
            log.debug("Task service JWT parsed, userId={}, username={}", userId, username);
            return new AuthUser(userId, username, token);
        } catch (JwtException | IllegalArgumentException exception) {
            log.warn("Task service JWT parse failed, reason={}", exception.getClass().getSimpleName());
            throw BusinessException.unauthorized("Invalid or expired token");
        }
    }
}
