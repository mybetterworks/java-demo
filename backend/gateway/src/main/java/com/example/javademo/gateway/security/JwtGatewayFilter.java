package com.example.javademo.gateway.security;

import com.example.javademo.gateway.common.GatewayApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * Gateway 全局 JWT 校验过滤器。
 *
 * <p>该过滤器是 v0.5 的核心：所有进入网关的请求都会先经过这里。
 * 登录、注册、健康检查和接口文档属于公开路径，会直接放行；用户管理等受保护接口必须携带
 * Authorization: Bearer {token}。校验通过后请求继续转发到 backend/app，后端自己的拦截器
 * 仍会再次校验 JWT，用来演示“网关入口校验 + 服务自身防线”的双层保护。</p>
 */
@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    /** HTTP Authorization 头中 Bearer token 的标准前缀。 */
    private static final String BEARER_PREFIX = "Bearer ";

    /** 精确匹配的公开路径，这些路径不需要登录即可访问。 */
    private static final Set<String> PUBLIC_EXACT_PATHS = Set.of(
            "/api/health",
            "/api/auth/register",
            "/api/auth/login",
            "/v3/api-docs",
            "/swagger-ui.html",
            "/favicon.ico",
            "/actuator/health"
    );

    /** 前缀匹配的公开路径，主要用于 Swagger UI 静态资源和 OpenAPI 扩展路径。 */
    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/v3/api-docs/",
            "/swagger-ui/",
            "/webjars/"
    );

    /** 传递给下游的网关认证头。先删除再写入，避免客户端伪造同名头。 */
    private static final String GATEWAY_USER_ID_HEADER = "X-Gateway-User-Id";
    private static final String GATEWAY_USERNAME_HEADER = "X-Gateway-Username";

    private final JwtVerifier jwtVerifier;
    private final ObjectMapper objectMapper;

    public JwtGatewayFilter(JwtVerifier jwtVerifier, ObjectMapper objectMapper) {
        this.jwtVerifier = jwtVerifier;
        this.objectMapper = objectMapper;
    }

    /**
     * 执行 JWT 校验的过滤器方法。
     *
     * <p>该方法会拦截所有进入网关的请求，判断是否需要进行身份认证。
     * OPTIONS 请求通常是浏览器跨域预检请求，必须直接放行；否则浏览器还没发送真实请求，预检就会被 401 拦住，前端无法完成跨域调用。
     * 如果请求路径属于公开路径或是 OPTIONS 请求，则直接放行。
     * 否则，会检查请求头中的 Authorization 是否携带有效的 Bearer Token。
     * 如果校验通过，则将认证信息添加到请求头中，并将请求转发到下游服务。
     * 如果校验失败，则返回 401 未授权响应。</p>
     *
     * @param exchange 当前请求的上下文，包含请求和响应对象
     * @param chain    过滤器链，将请求传递给下一个过滤器
     * @return 返回一个 Mono<Void>，表示异步处理的结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取当前请求对象
        ServerHttpRequest request = exchange.getRequest();

        // 如果是 OPTIONS 请求（通常是浏览器的跨域预检请求），直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod().name())) {
            return chain.filter(exchange);
        }

        // 获取请求路径
        String path = request.getURI().getPath();

        // 如果请求路径不需要认证，直接放行
        if (!requiresAuthentication(path)) {
            return chain.filter(exchange);
        }

        // 获取请求头中的 Authorization 字段
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // 如果 Authorization 为空或不是以 "Bearer " 开头，返回 401 未授权响应
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return writeUnauthorized(exchange, "Missing bearer token");
        }

        // 提取 Bearer Token（去掉 "Bearer " 前缀并去除多余空格）
        String token = authorization.substring(BEARER_PREFIX.length()).trim();

        // 如果 Token 为空，返回 401 未授权响应
        if (token.isEmpty()) {
            return writeUnauthorized(exchange, "Missing bearer token");
        }

        try {
            // 验证 Token 并解析出用户信息
            GatewayAuthUser authUser = jwtVerifier.verify(token);

            // 构建一个新的请求对象，添加认证用户信息到请求头中
            ServerHttpRequest authenticatedRequest = request.mutate()
                    .headers(headers -> {
                        // 先移除可能存在的伪造认证头
                        headers.remove(GATEWAY_USER_ID_HEADER);
                        headers.remove(GATEWAY_USERNAME_HEADER);
                    })
                    // 添加认证用户的 ID 和用户名到请求头
                    .header(GATEWAY_USER_ID_HEADER, String.valueOf(authUser.userId()))
                    .header(GATEWAY_USERNAME_HEADER, authUser.username() == null ? "" : authUser.username())
                    .build();

            // 将修改后的请求对象传递给下一个过滤器
            return chain.filter(exchange.mutate().request(authenticatedRequest).build());
        } catch (JwtAuthenticationException exception) {
            // 如果 Token 验证失败，返回 401 未授权响应
            return writeUnauthorized(exchange, exception.getMessage());
        }
    }

    /**
     * 过滤器顺序。
     *
     * <p>设置为较高优先级，让 JWT 校验尽量早发生，避免无效请求继续进入后续路由和转发逻辑。</p>
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    /**
     * 判断当前路径是否需要 JWT。
     *
     * <p>v0.5 只保护 /api 下的业务接口；Swagger 和健康检查保留公开访问，方便本地验收。</p>
     */
    private boolean requiresAuthentication(String path) {
        if (!path.startsWith("/api/")) {
            return false;
        }
        if (PUBLIC_EXACT_PATHS.contains(path)) {
            return false;
        }
        return PUBLIC_PREFIXES.stream().noneMatch(path::startsWith);
    }

    /**
     * 写出网关层 401 响应。
     *
     * <p>响应结构与后端 ApiResponse 保持一致，这样 React 和 Vue 端不用区分错误来自网关还是后端。</p>
     */
    private Mono<Void> writeUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(HttpHeaders.CACHE_CONTROL, "no-store");

        byte[] bytes = serializeResponse(message);
        DataBuffer dataBuffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(dataBuffer));
    }

    /**
     * 序列化错误响应。
     *
     * <p>正常情况下 ObjectMapper 不会序列化失败；如果真的失败，仍返回一个最小 JSON，
     * 避免客户端收到空响应后难以排查。</p>
     */
    private byte[] serializeResponse(String message) {
        try {
            return objectMapper.writeValueAsBytes(GatewayApiResponse.fail(401, message));
        } catch (JsonProcessingException exception) {
            return ("{\"code\":401,\"message\":\"Unauthorized\",\"data\":null}")
                    .getBytes(StandardCharsets.UTF_8);
        }
    }
}
