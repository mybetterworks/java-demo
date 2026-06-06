package com.example.javademo.app;

import com.example.javademo.app.dto.CreateUserRequest;
import com.example.javademo.app.service.UserManagementService;
import com.example.javademo.rpc.user.UserRpcService;
import com.example.javademo.rpc.user.UserSummaryRpcResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用户 Dubbo provider 集成测试。
 *
 * <p>这里不走真实网络，只验证 provider 本身是否正确复用了现有用户管理领域逻辑：
 * 用户存在时返回最小摘要，用户被逻辑删除后返回 null，
 * 从而保证 v0.6.2 的 Dubbo 语义与原有 HTTP 用户查询语义保持一致。</p>
 */
@ActiveProfiles("test")
@SpringBootTest
class UserRpcServiceIntegrationTest {

    @Autowired
    private UserRpcService userRpcService;

    @Autowired
    private UserManagementService userManagementService;

    @Test
    void shouldReturnUserSummaryForExistingUser() {
        String suffix = Long.toString(System.nanoTime());
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("rpc_user_" + suffix);
        request.setPassword("rpcSecret123");
        request.setNickname("RPC User");
        request.setStatus(1);
        request.setRole("USER");

        Long userId = userManagementService.createUser(request).getId();
        UserSummaryRpcResponse response = userRpcService.getUserSummary(userId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo("rpc_user_" + suffix);
        assertThat(response.getStatus()).isEqualTo(1);
    }

    @Test
    void shouldReturnNullWhenUserWasDeleted() {
        String suffix = Long.toString(System.nanoTime());
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("rpc_deleted_" + suffix);
        request.setPassword("rpcSecret123");
        request.setNickname("RPC Deleted");
        request.setStatus(1);
        request.setRole("USER");

        Long userId = userManagementService.createUser(request).getId();
        userManagementService.deleteUser(userId);

        assertThat(userRpcService.getUserSummary(userId)).isNull();
    }
}
