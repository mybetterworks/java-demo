package com.example.javademo.app.controller;

import com.example.javademo.app.common.ApiResponse;
import com.example.javademo.app.dto.LoginRequest;
import com.example.javademo.app.dto.LoginResponse;
import com.example.javademo.app.dto.RegisterRequest;
import com.example.javademo.app.dto.UserProfileResponse;
import com.example.javademo.app.service.UserAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证相关接口。
 *
 * <p>v0.1 只包含注册和登录两个入口。Controller 负责接收 HTTP 请求和返回统一响应，
 * 具体的用户名规范化、密码哈希、JWT 签发等业务逻辑都放在 UserAccountService 中。</p>
 */
@Tag(name = "Auth", description = "注册、登录和 JWT 签发接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserAccountService userAccountService;

    public AuthController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    /**
     * 注册新用户。
     *
     * @param request 注册请求，包含用户名、密码和可选昵称
     * @return 注册成功后的用户基础信息，不包含密码哈希
     */
    @Operation(summary = "注册用户", description = "创建本地用户，密码会以 BCrypt 哈希保存。")
    @PostMapping("/register")
    public ApiResponse<UserProfileResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("registered", userAccountService.register(request));
    }

    /**
     * 用户登录。
     *
     * @param request 登录请求，包含用户名和密码
     * @return 登录成功后的 JWT、过期时间和当前用户信息
     */
    @Operation(summary = "用户登录", description = "校验用户名和密码，成功后返回 Bearer JWT。")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("logged in", userAccountService.login(request));
    }
}
