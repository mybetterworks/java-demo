package com.example.javademo.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * v0.2 用户管理集成测试。
 *
 * <p>该测试在随机端口启动完整应用，通过 HTTP 调用真实 Controller。
 * 它覆盖用户管理的核心验收标准，确保新增 CRUD 能力不会破坏 v0.1 登录和 JWT 鉴权链路。</p>
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserManagementIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldManageUsersAfterLogin() throws Exception {
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        String suffix = Long.toString(System.nanoTime());
        String adminUsername = "admin_" + suffix;
        String managedUsername = "managed_" + suffix;

        String token = registerAndLogin(adminUsername, "adminSecret123");
        HttpHeaders authHeaders = jsonHeaders();
        authHeaders.setBearerAuth(token);

        // 1. 未登录访问用户管理接口必须被拦截，避免管理能力裸奔。
        ResponseEntity<String> noTokenListResponse = restTemplate.getForEntity("/api/users", String.class);
        assertThat(noTokenListResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // 2. 登录后可以创建新用户，角色会被服务端规范化为大写。
        Map<String, Object> createRequest = Map.of(
                "username", managedUsername,
                "password", "oldSecret123",
                "nickname", "Managed User",
                "status", 1,
                "role", "operator"
        );
        ResponseEntity<String> createResponse = restTemplate.exchange(
                "/api/users",
                HttpMethod.POST,
                new HttpEntity<>(createRequest, authHeaders),
                String.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode createdJson = readJson(createResponse);
        long managedUserId = createdJson.path("data").path("id").asLong();
        assertThat(createdJson.path("data").path("username").asText()).isEqualTo(managedUsername);
        assertThat(createdJson.path("data").path("role").asText()).isEqualTo("OPERATOR");

        // 3. 支持按用户名和状态分页查询，且默认不会返回已逻辑删除用户。
        ResponseEntity<String> listResponse = restTemplate.exchange(
                "/api/users?current=1&size=10&username=" + managedUsername + "&status=1",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders),
                String.class
        );
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode listJson = readJson(listResponse);
        assertThat(listJson.path("data").path("total").asLong()).isGreaterThanOrEqualTo(1);
        assertThat(recordsContainUserId(listJson, managedUserId)).isTrue();

        // 4. 用户详情可以按 ID 查询。
        ResponseEntity<String> detailResponse = restTemplate.exchange(
                "/api/users/" + managedUserId,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders),
                String.class
        );
        assertThat(detailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readJson(detailResponse).path("data").path("id").asLong()).isEqualTo(managedUserId);

        // 5. 可以更新昵称、状态和角色，用户名和密码不在该接口里修改。
        Map<String, Object> updateRequest = Map.of(
                "nickname", "Managed Renamed",
                "status", 1,
                "role", "admin"
        );
        ResponseEntity<String> updateResponse = restTemplate.exchange(
                "/api/users/" + managedUserId,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest, authHeaders),
                String.class
        );
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode updatedJson = readJson(updateResponse);
        assertThat(updatedJson.path("data").path("nickname").asText()).isEqualTo("Managed Renamed");
        assertThat(updatedJson.path("data").path("role").asText()).isEqualTo("ADMIN");

        // 6. 修改密码后，旧密码不能登录，新密码可以登录。
        Map<String, String> passwordRequest = Map.of("password", "newSecret123");
        ResponseEntity<String> changePasswordResponse = restTemplate.exchange(
                "/api/users/" + managedUserId + "/password",
                HttpMethod.PUT,
                new HttpEntity<>(passwordRequest, authHeaders),
                String.class
        );
        assertThat(changePasswordResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginStatus(managedUsername, "oldSecret123")).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(loginStatus(managedUsername, "newSecret123")).isEqualTo(HttpStatus.OK.value());

        // 7. 逻辑删除后，详情不可查、列表不可见、登录也不可用。
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                "/api/users/" + managedUserId,
                HttpMethod.DELETE,
                new HttpEntity<>(authHeaders),
                String.class
        );
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> deletedDetailResponse = restTemplate.exchange(
                "/api/users/" + managedUserId,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders),
                String.class
        );
        assertThat(deletedDetailResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> afterDeleteListResponse = restTemplate.exchange(
                "/api/users?current=1&size=10&username=" + managedUsername,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders),
                String.class
        );
        assertThat(afterDeleteListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(recordsContainUserId(readJson(afterDeleteListResponse), managedUserId)).isFalse();
        assertThat(loginStatus(managedUsername, "newSecret123")).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    /**
     * 注册并登录一个测试用户，返回可用于后续接口的 JWT。
     */
    private String registerAndLogin(String username, String password) throws Exception {
        Map<String, String> registerRequest = Map.of(
                "username", username,
                "password", password,
                "nickname", username
        );
        ResponseEntity<String> registerResponse = restTemplate.postForEntity("/api/auth/register", registerRequest, String.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, String> loginRequest = Map.of(
                "username", username,
                "password", password
        );
        ResponseEntity<String> loginResponse = restTemplate.postForEntity("/api/auth/login", loginRequest, String.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        return readJson(loginResponse).path("data").path("accessToken").asText();
    }

    /**
     * 返回登录请求的 HTTP 状态码，用于验证改密和逻辑删除后的登录行为。
     */
    private int loginStatus(String username, String password) {
        Map<String, String> loginRequest = Map.of(
                "username", username,
                "password", password
        );
        return restTemplate.postForEntity("/api/auth/login", loginRequest, String.class).getStatusCode().value();
    }

    /**
     * 判断分页响应 records 中是否包含指定用户 ID。
     */
    private boolean recordsContainUserId(JsonNode pageJson, long userId) {
        for (JsonNode record : pageJson.path("data").path("records")) {
            if (record.path("id").asLong() == userId) {
                return true;
            }
        }
        return false;
    }

    /**
     * 创建 JSON 请求头，后续再按需追加 Authorization。
     */
    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * 将 HTTP 响应体解析为 JsonNode，方便断言嵌套字段。
     */
    private JsonNode readJson(ResponseEntity<String> response) throws Exception {
        return objectMapper.readTree(response.getBody());
    }
}
