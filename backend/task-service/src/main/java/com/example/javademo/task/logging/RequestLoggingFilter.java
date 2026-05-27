package com.example.javademo.task.logging;

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
 * 任务服务请求日志过滤器。
 *
 * <p>Filter 比 HandlerInterceptor 更靠前，能让认证拦截、Controller、Service 和异常处理日志
 * 都共享同一个 requestId，便于排查 Gateway -> task-service -> notification-service 链路。</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String MDC_REQUEST_ID = "requestId";

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    /**
     * 记录任务服务 HTTP 请求的开始和结束。
     *
     * <p>日志只包含 path、状态码和耗时，不记录 Authorization header、完整 token 或请求体。</p>
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
            // 清理 MDC 是 requestId 正确性的关键，否则 Tomcat 线程复用时可能把上一个请求的标识带到下一个请求。
            long durationMs = System.currentTimeMillis() - startTime;
            log.info("HTTP request completed, method={}, path={}, status={}, durationMs={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs);
            MDC.remove(MDC_REQUEST_ID);
        }
    }

    /**
     * 优先复用外部传入的 requestId；缺失或非法时生成新的本地 requestId。
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
