package com.example.javademo.app.controller;

import com.example.javademo.app.common.ApiResponse;
import com.example.javademo.app.config.OpenApiConfig;
import com.example.javademo.app.dto.ChangePasswordRequest;
import com.example.javademo.app.dto.CreateUserRequest;
import com.example.javademo.app.dto.PageResponse;
import com.example.javademo.app.dto.UpdateUserRequest;
import com.example.javademo.app.dto.UserProfileResponse;
import com.example.javademo.app.security.CurrentUserContext;
import com.example.javademo.app.service.UserAccountService;
import com.example.javademo.app.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息与用户管理接口。
 *
 * <p>请求进入 Controller 前已经由 AuthInterceptor 解析 token 并写入 CurrentUserContext。
 * v0.2 在 /api/users/me 基础上增加管理型 CRUD 接口。当前还没有复杂 RBAC，
 * 因此所有已登录用户都可以调用这些接口；真正的角色权限会留到后续独立 milestone 再做。</p>
 */
@Tag(name = "Users", description = "用户信息接口")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserAccountService userAccountService;
    private final UserManagementService userManagementService;

    public UserController(UserAccountService userAccountService, UserManagementService userManagementService) {
        this.userAccountService = userAccountService;
        this.userManagementService = userManagementService;
    }

    /**
     * 分页查询用户。
     *
     * @return 用户分页数据，默认只包含未逻辑删除用户
     */
    @Operation(summary = "用户分页查询", description = "支持按用户名关键字和用户状态筛选，默认过滤已逻辑删除用户。")
    @GetMapping
    public ApiResponse<PageResponse<UserProfileResponse>> pageUsers(
            @Parameter(description = "当前页码，从 1 开始") @RequestParam(name = "current", defaultValue = "1") Long current,
            @Parameter(description = "每页条数，最大 100") @RequestParam(name = "size", defaultValue = "10") Long size,
            @Parameter(description = "用户名关键字，可选") @RequestParam(name = "username", required = false) String username,
            @Parameter(description = "用户状态，1 启用，0 禁用") @RequestParam(name = "status", required = false) Integer status) {
        return ApiResponse.success(userManagementService.pageUsers(current, size, username, status));
    }

    /**
     * 根据 ID 查询用户详情。
     */
    @Operation(summary = "用户详情", description = "根据用户 ID 查询未删除用户详情。")
    @GetMapping("/{id:\\d+}")
    public ApiResponse<UserProfileResponse> getUser(@PathVariable("id") Long id) {
        return ApiResponse.success(userManagementService.getUser(id));
    }

    /**
     * 管理端创建用户。
     */
    @Operation(summary = "创建用户", description = "管理端创建用户，密码会以 BCrypt 哈希保存。")
    @PostMapping
    public ApiResponse<UserProfileResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.success("created", userManagementService.createUser(request));
    }

    /**
     * 更新用户基础信息。
     */
    @Operation(summary = "更新用户", description = "修改昵称、状态和角色等基础字段，不修改用户名和密码。")
    @PutMapping("/{id:\\d+}")
    public ApiResponse<UserProfileResponse> updateUser(@PathVariable("id") Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.success("updated", userManagementService.updateUser(id, request));
    }

    /**
     * 逻辑删除用户。
     */
    @Operation(summary = "删除用户", description = "逻辑删除用户，删除后默认列表、详情和登录查询都不可见。")
    @DeleteMapping("/{id:\\d+}")
    public ApiResponse<Void> deleteUser(@PathVariable("id") Long id) {
        userManagementService.deleteUser(id);
        return ApiResponse.success("deleted", null);
    }

    /**
     * 修改用户密码。
     */
    @Operation(summary = "修改密码", description = "独立修改用户密码，新密码会重新生成 BCrypt 哈希。")
    @PutMapping("/{id:\\d+}/password")
    public ApiResponse<Void> changePassword(@PathVariable("id") Long id, @Valid @RequestBody ChangePasswordRequest request) {
        userManagementService.changePassword(id, request);
        return ApiResponse.success("password changed", null);
    }

    /**
     * 查询当前登录用户。
     *
     * @return token 对应用户的基础资料
     */
    @Operation(summary = "获取当前登录用户", description = "通过 Authorization Bearer JWT 获取当前用户信息。")
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> me() {
        return ApiResponse.success(userAccountService.getProfile(CurrentUserContext.getRequired()));
    }
}
