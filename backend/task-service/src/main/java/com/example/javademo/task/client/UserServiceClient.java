package com.example.javademo.task.client;

import com.example.javademo.task.common.ApiResponse;
import com.example.javademo.task.common.BusinessException;
import com.example.javademo.task.config.ServiceClientProperties;
import com.example.javademo.task.security.AuthUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * 用户服务 REST 客户端。
 *
 * <p>v0.6 之后默认通过服务名 http://java-demo-app 访问用户服务。调用时继续转发当前登录用户的 JWT，
 * 但日志只记录业务 ID、耗时和目标服务地址，不打印 token 或 Authorization header。</p>
 */
@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    /** 与请求日志过滤器保持一致，服务间调用时透传该值，便于跨服务日志串联。 */
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final RestTemplate restTemplate;
    private final ServiceClientProperties properties;

    public UserServiceClient(RestTemplate restTemplate, ServiceClientProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    /**
     * 校验负责人的用户确实存在。
     *
     * @param userId 待校验用户 ID
     * @param currentUser 当前登录用户，用于透传 JWT
     * @return 用户服务返回的最小用户信息
     */
    public UserProfileResponse requireUser(Long userId, AuthUser currentUser) {
        String baseUrl = trimTrailingSlash(properties.getUserServiceUrl());
        String url = baseUrl + "/api/users/" + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getAccessToken());
        attachRequestId(headers);
        long startTime = System.currentTimeMillis();

        try {
            // 服务间调用日志只记录业务 ID、操作人和目标地址，避免把 JWT 等敏感内容写入日志。
            log.info("Calling user service to validate assignee, assigneeUserId={}, operatorUserId={}, target={}",
                    userId, currentUser.getId(), sanitizeTarget(baseUrl));
            ResponseEntity<ApiResponse<UserProfileResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<ApiResponse<UserProfileResponse>>() {
                    }
            );
            ApiResponse<UserProfileResponse> body = response.getBody();
            if (body == null || body.getCode() != 0 || body.getData() == null || body.getData().getId() == null) {
                throw BusinessException.downstream("User service returned invalid response");
            }
            log.info("User service validation succeeded, assigneeUserId={}, status={}, durationMs={}, target={}",
                    userId, response.getStatusCode().value(), System.currentTimeMillis() - startTime, sanitizeTarget(baseUrl));
            return body.getData();
        } catch (HttpStatusCodeException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw BusinessException.badRequest("Assignee user does not exist");
            }
            if (exception.getStatusCode().value() == 401) {
                throw BusinessException.unauthorized("User service rejected current token");
            }
            log.warn("User service validation failed, assigneeUserId={}, status={}, target={}",
                    userId, exception.getStatusCode().value(), sanitizeTarget(baseUrl));
            throw BusinessException.downstream("User service is unavailable");
        } catch (RestClientException exception) {
            log.warn("User service call failed, assigneeUserId={}, reason={}, target={}",
                    userId, exception.getClass().getSimpleName(), sanitizeTarget(baseUrl));
            throw BusinessException.downstream("User service is unavailable");
        }
    }

    /**
     * 透传当前请求的 requestId，方便把任务服务和用户服务日志串起来。
     */
    private void attachRequestId(HttpHeaders headers) {
        String requestId = MDC.get("requestId");
        if (requestId != null && !requestId.isBlank()) {
            headers.set(REQUEST_ID_HEADER, requestId);
        }
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://java-demo-app";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    /**
     * 对目标地址做兜底脱敏。
     *
     * <p>当前地址通常只是服务名或本地调试 URL，但这里仍然保护 password/pwd 参数，避免后续扩展时误打日志。</p>
     */
    private String sanitizeTarget(String value) {
        return value
                .replaceAll("(?i)(password=)[^&;]+", "$1****")
                .replaceAll("(?i)(pwd=)[^&;]+", "$1****");
    }
}
