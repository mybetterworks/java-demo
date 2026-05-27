package com.example.javademo.notification.logging;

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
 * 通知服务请求日志过滤器。
 *
 * <p>该过滤器负责生成 requestId 并记录通知服务的 HTTP 请求边界，让通知创建、查询和已读操作
 * 可以在控制台和文件日志中快速定位。</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String MDC_REQUEST_ID = "requestId";

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    /**
     * 记录请求入口、响应状态和耗时。
     *
     * <p>通知内容未来可能包含用户输入或长文本，所以请求日志不打印请求体，只打印可定位链路的摘要信息。</p>
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
            // 请求完成后清理 MDC，保证不同通知请求之间的 requestId 不会互相污染。
            long durationMs = System.currentTimeMillis() - startTime;
            log.info("HTTP request completed, method={}, path={}, status={}, durationMs={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs);
            MDC.remove(MDC_REQUEST_ID);
        }
    }

    /**
     * 解析可信 requestId；非法值会被丢弃并重新生成。
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
