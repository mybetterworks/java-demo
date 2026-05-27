package com.example.javademo.task.client;

import com.example.javademo.task.common.ApiResponse;
import com.example.javademo.task.common.BusinessException;
import com.example.javademo.task.config.ServiceClientProperties;
import com.example.javademo.task.security.AuthUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

        try {
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

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:8091";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
