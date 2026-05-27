package com.example.javademo.notification.security;

import com.example.javademo.notification.common.BusinessException;
import com.example.javademo.notification.config.JwtProperties;
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
 * 通知服务 JWT 解析服务。
 *
 * <p>JWT 仍由 java-demo-app 签发，通知服务只负责验签和解析。这样即使绕过 Gateway 直连通知服务，
 * 服务自身仍然具备认证防线。</p>
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
            // DEBUG 只记录认证主体摘要，不记录完整 JWT。
            log.debug("Notification service JWT parsed, userId={}, username={}", userId, username);
            return new AuthUser(userId, username, token);
        } catch (JwtException | IllegalArgumentException exception) {
            log.warn("Notification service JWT parse failed, reason={}", exception.getClass().getSimpleName());
            throw BusinessException.unauthorized("Invalid or expired token");
        }
    }
}
