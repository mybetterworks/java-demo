package com.example.javademo.app.controller;

import com.example.javademo.app.common.ApiResponse;
import com.example.javademo.app.dto.UserProfileResponse;
import com.example.javademo.app.config.OpenApiConfig;
import com.example.javademo.app.security.CurrentUserContext;
import com.example.javademo.app.service.UserAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息接口。
 *
 * <p>当前版本只开放 /api/users/me，用于验证 JWT 认证链路是否完整。
 * 请求进入 Controller 前已经由 AuthInterceptor 解析 token 并写入 CurrentUserContext。</p>
 */
@Tag(name = "Users", description = "用户信息接口")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserAccountService userAccountService;

    public UserController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    /**
     * 查询当前登录用户。
     *
     * @return token 对应用户的基础资料
     */
    @Operation(summary = "获取当前登录用户", description = "通过 Authorization Bearer JWT 获取当前用户信息。")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> me() {
        return ApiResponse.success(userAccountService.getProfile(CurrentUserContext.getRequired()));
    }
}
