package com.example.javademo.app.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 用户服务请求日志过滤器。
 *
 * <p>v0.5.2 先用轻量 Filter 生成 requestId 并写入 MDC，让同一次 HTTP 请求中的认证、
 * 业务和异常日志都带上同一个标识。后续接入 SkyWalking 或 TraceId 时，可以在这里平滑替换。</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    /** 外部调用方可以传入该请求头复用 requestId；未传时服务端自动生成。 */
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    /** MDC 中 requestId 的 key，需要和 application.yml 的日志 pattern 保持一致。 */
    private static final String MDC_REQUEST_ID = "requestId";

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    /**
     * 为每个 HTTP 请求增加 requestId、入口日志和完成日志。
     *
     * <p>这里故意只记录 method、path、status 和耗时，不记录 Authorization、完整 query string
     * 或请求体，避免把 token、密码、长文本等敏感信息写入日志文件。</p>
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String requestId = resolveRequestId(request);
        MDC.put(MDC_REQUEST_ID, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            log.info("HTTP request started, method={}, path={}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束时统一记录状态码和耗时，并清理 MDC，避免 Servlet 线程复用导致 requestId 串号。
            long durationMs = System.currentTimeMillis() - startTime;
            log.info("HTTP request completed, method={}, path={}, status={}, durationMs={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs);
            MDC.remove(MDC_REQUEST_ID);
        }
    }

    /**
     * 解析或生成 requestId。
     *
     * <p>来自请求头的 requestId 会先做长度和字符白名单校验，避免日志换行注入或超长字段污染日志。</p>
     */
    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId != null) {
            String normalized = requestId.trim();
            if (!normalized.isEmpty() && normalized.length() <= 64 && normalized.matches("[A-Za-z0-9._-]+")) {
                return normalized;
            }
        }
        return UUID.randomUUID().toString().replace("-", "");
    }
}
