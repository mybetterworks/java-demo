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
 * <p>v0.5.1 使用静态地址调用 java-demo-app 的用户详情接口，目的只是确认负责人用户真实存在。
 * 调用时转发当前登录用户的 JWT，但日志只记录用户 ID 和状态，不打印 token。</p>
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
     * 校验负责人用户存在。
     *
     * @param userId      待校验用户 ID
     * @param currentUser 当前登录用户，用于转发 JWT
     * @return 用户服务返回的最小用户信息
     */
    public UserProfileResponse requireUser(Long userId, AuthUser currentUser) {
        String url = trimTrailingSlash(properties.getUserServiceUrl()) + "/api/users/" + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getAccessToken());
        attachRequestId(headers);
        long startTime = System.currentTimeMillis();

        try {
            // 服务间调用日志只记录业务 ID 和操作者 ID，不输出 token 或 Authorization header。
            log.info("Calling user service to validate assignee, assigneeUserId={}, operatorUserId={}", userId, currentUser.getId());
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
            log.info("User service validation succeeded, assigneeUserId={}, status={}, durationMs={}",
                    userId, response.getStatusCode().value(), System.currentTimeMillis() - startTime);
            return body.getData();
        } catch (HttpStatusCodeException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw BusinessException.badRequest("Assignee user does not exist");
            }
            if (exception.getStatusCode().value() == 401) {
                throw BusinessException.unauthorized("User service rejected current token");
            }
            log.warn("User service validation failed, assigneeUserId={}, status={}", userId, exception.getStatusCode().value());
            throw BusinessException.downstream("User service is unavailable");
        } catch (RestClientException exception) {
            log.warn("User service call failed, assigneeUserId={}, reason={}", userId, exception.getClass().getSimpleName());
            throw BusinessException.downstream("User service is unavailable");
        }
    }

    /**
     * 透传当前请求的 requestId。
     *
     * <p>task-service 同步调用 java-demo-app 时，透传 requestId 可以让两个服务的文件日志按同一请求串起来；
     * 如果当前线程没有 requestId，则保持为空，不额外生成新的链路标识。</p>
     */
    private void attachRequestId(HttpHeaders headers) {
        String requestId = MDC.get("requestId");
        if (requestId != null && !requestId.isBlank()) {
            headers.set(REQUEST_ID_HEADER, requestId);
        }
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:8091";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
