package com.example.javademo.gateway.security;

import com.example.javademo.gateway.config.JwtProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Gateway JWT 过滤器测试。
 *
 * <p>该测试不启动真实后端，也不依赖 MySQL。我们只验证网关最核心的行为：
 * 公开路径直接放行、受保护路径无 token 返回 401、有效 token 可以继续进入路由链。
 * 这样 v0.5 的网关能力即使在离线环境中也能被快速回归。</p>
 */
class JwtGatewayFilterTest {

    // 测试用的 JWT 密钥，长度必须为 32 个字符
    private static final String TEST_SECRET = "java-demo-test-secret-change-me-32chars";

    /**
     * 测试：公开路径（如登录接口）在没有 Token 的情况下可以直接访问，返回 200 OK。
     */
    @Test
    void shouldAllowPublicLoginPathWithoutToken() {
        // 创建过滤器实例
        JwtGatewayFilter filter = createFilter();
        // 模拟一个 POST 请求到 "/api/auth/login"
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/auth/login").build()
        );
        // 用于验证过滤器链是否被调用
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        // 执行过滤器逻辑
        filter.filter(exchange, passChain(chainCalled)).block();

        // 验证过滤器链被调用，且响应状态为 200 OK
        assertThat(chainCalled).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /**
     * 测试：任务和通知服务的健康检查属于公开路径，方便 Gateway 路由和后续注册中心探测。
     */
    @Test
    void shouldAllowServiceHealthPathsWithoutToken() {
        JwtGatewayFilter filter = createFilter();

        MockServerWebExchange taskHealthExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/tasks/health").build()
        );
        AtomicBoolean taskChainCalled = new AtomicBoolean(false);
        filter.filter(taskHealthExchange, passChain(taskChainCalled)).block();
        assertThat(taskChainCalled).isTrue();
        assertThat(taskHealthExchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);

        MockServerWebExchange notificationHealthExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/notifications/health").build()
        );
        AtomicBoolean notificationChainCalled = new AtomicBoolean(false);
        filter.filter(notificationHealthExchange, passChain(notificationChainCalled)).block();
        assertThat(notificationChainCalled).isTrue();
        assertThat(notificationHealthExchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /**
     * 测试：受保护路径在没有 Token 的情况下会被拒绝访问，返回 401 未授权。
     */
    @Test
    void shouldRejectProtectedPathWithoutToken() {
        // 创建过滤器实例
        JwtGatewayFilter filter = createFilter();
        // 模拟一个 GET 请求到 "/api/users"
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users").build()
        );
        // 用于验证过滤器链是否被调用
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        // 执行过滤器逻辑
        filter.filter(exchange, passChain(chainCalled)).block();

        // 验证过滤器链未被调用，且响应状态为 401 未授权
        assertThat(chainCalled).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("Missing bearer token");
    }

    /**
     * 测试：新增任务接口仍属于受保护业务接口，不带 Token 时由网关直接拒绝。
     */
    @Test
    void shouldRejectTaskApiWithoutToken() {
        JwtGatewayFilter filter = createFilter();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/tasks").build()
        );
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.filter(exchange, passChain(chainCalled)).block();

        assertThat(chainCalled).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /**
     * 测试：受保护路径在携带有效 Token 的情况下可以正常访问，并且会附加网关认证头。
     */
    @Test
    void shouldAllowProtectedPathWithValidTokenAndAppendGatewayHeaders() {
        // 创建过滤器实例
        JwtGatewayFilter filter = createFilter();
        // 模拟一个 GET 请求到 "/api/users"，并携带有效的 Bearer Token 和伪造的网关头
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + createToken()) // 添加有效 Token
                        .header("X-Gateway-User-Id", "fake") // 添加伪造的网关头
                        .build()
        );
        // 用于存储过滤后的请求上下文
        AtomicReference<ServerWebExchange> routedExchange = new AtomicReference<>();

        // 执行过滤器逻辑
        filter.filter(exchange, exchangeAfterFilter -> {
            // 保存过滤后的请求上下文
            routedExchange.set(exchangeAfterFilter);
            // 设置响应状态为 200 OK
            exchangeAfterFilter.getResponse().setStatusCode(HttpStatus.OK);
            return exchangeAfterFilter.getResponse().setComplete();
        }).block();

        // 验证响应状态为 200 OK
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
        // 验证过滤后的请求上下文为空
        assertThat(routedExchange.get()).isNotNull();
        // 验证网关头被正确设置为解析后的用户信息
        assertThat(routedExchange.get().getRequest().getHeaders().getFirst("X-Gateway-User-Id")).isEqualTo("1001");
        assertThat(routedExchange.get().getRequest().getHeaders().getFirst("X-Gateway-Username")).isEqualTo("gateway_user");
    }

    /**
     * 测试：受保护路径在携带无效 Token 的情况下会被拒绝访问，返回 401 未授权。
     */
    @Test
    void shouldRejectProtectedPathWithInvalidToken() {
        // 创建过滤器实例
        JwtGatewayFilter filter = createFilter();
        // 模拟拟一个 GET 请求到 "/api/users"，并携带无效的 Bearer Token
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer broken-token") // 添加无效 Token
                        .build()
        );
        // 用于验证过滤器链是否被调用
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        // 执行过滤器逻辑
        filter.filter(exchange, passChain(chainCalled)).block();

        // 验证过滤器链未被调用，且响应状态为 401 未授权
        assertThat(chainCalled).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("Invalid or expired token");
    }

    /**
     * 创建被测过滤器。
     *
     * <p>这里直接实例化组件，而不是启动 Spring 容器，目的是让测试只关注 JWT 过滤逻辑本身。</p>
     */
    private JwtGatewayFilter createFilter() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(TEST_SECRET); // 设置测试用的密钥
        properties.setExpirationSeconds(7200); // 设置 Token 过期时间为 7200 秒
        return new JwtGatewayFilter(new JwtVerifier(properties), new ObjectMapper());
    }

    /**
     * 构造一个代表“请求继续向后路由”的过滤器链。
     *
     * @param chainCalled 用于记录过滤器链是否被调用
     * @return 一个模拟的过滤器链
     */
    private GatewayFilterChain passChain(AtomicBoolean chainCalled) {
        return exchange -> {
            chainCalled.set(true); // 标记过滤器链被调用
            exchange.getResponse().setStatusCode(HttpStatus.OK); // 设置响应状态为 200 OK
            return exchange.getResponse().setComplete();
        };
    }

    /**
     * 使用和后端相同的 HS256 规则生成测试 Token。
     *
     * @return 生成的测试 Token
     */
    private String createToken() {
        // 使用测试密钥生成签名密钥
        SecretKey signingKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now(); // 获取当前时间
        // 构建 JWT Token
        return Jwts.builder()
                .subject("1001") // 设置用户 ID
                .claim("username", "gateway_user") // 设置用户名
                .issuedAt(Date.from(now)) // 设置签发时间
                .expiration(Date.from(now.plusSeconds(7200))) // 设置过期时间
                .signWith(signingKey, Jwts.SIG.HS256) // 使用 HS256 算法签名
                .compact(); // 生成 Token
    }
}
