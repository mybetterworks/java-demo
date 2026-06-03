package com.example.javademo.app.controller;

import com.example.javademo.app.common.ApiResponse;
import com.example.javademo.app.dto.LoginRequest;
import com.example.javademo.app.dto.LoginResponse;
import com.example.javademo.app.dto.RegisterRequest;
import com.example.javademo.app.dto.SliderCaptchaChallengeRequest;
import com.example.javademo.app.dto.SliderCaptchaChallengeResponse;
import com.example.javademo.app.dto.SliderCaptchaVerifyRequest;
import com.example.javademo.app.dto.SliderCaptchaVerifyResponse;
import com.example.javademo.app.dto.UserProfileResponse;
import com.example.javademo.app.service.UserAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证相关接口。
 *
 * <p>v0.1 只包含注册和登录两个入口。v0.5.4 增加滑块验证码 challenge 和 verify 入口。
 * Controller 负责接收 HTTP 请求和返回统一响应，具体的用户名规范化、密码哈希、登录风险判断、
 * 验证码状态和 JWT 签发等业务逻辑都放在 UserAccountService 中。</p>
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
     * @param request 登录请求，包含用户名、密码，以及风险状态下的一次性验证码 token
     * @param httpRequest HTTP 请求对象，用于解析 clientIp 并参与登录风险统计
     * @return 登录成功后的 JWT、过期时间和当前用户信息
     */
    @Operation(summary = "用户登录", description = "低风险时校验用户名和密码；高风险时需要先完成滑块验证码，再返回 Bearer JWT。")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success("logged in", userAccountService.login(request, resolveClientIp(httpRequest)));
    }

    /**
     * 创建滑块验证码 challenge。
     *
     * <p>该接口是公开接口，但只返回 challengeId 和滑块 UI 参数，不返回验证码答案或验证码 token。
     * 前端在登录失败触发验证码后调用它，随后把用户拖动结果提交到 verify 接口。</p>
     */
    @Operation(summary = "创建滑块验证码", description = "生成一次性滑块验证码 challenge，用于登录风险二次验证。")
    @PostMapping("/captcha/slider")
    public ApiResponse<SliderCaptchaChallengeResponse> createSliderCaptcha(
            @Valid @RequestBody SliderCaptchaChallengeRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                "captcha challenge created",
                userAccountService.createSliderCaptcha(request.getUsername(), resolveClientIp(httpRequest))
        );
    }

    /**
     * 校验滑块验证码。
     *
     * <p>校验成功后返回短 TTL、一次性 captchaToken。该 token 只允许随下一次登录请求提交，
     * 不能替代 JWT，也不会被日志记录。</p>
     */
    @Operation(summary = "校验滑块验证码", description = "校验滑块位置，成功后返回一次性验证码 token。")
    @PostMapping("/captcha/slider/verify")
    public ApiResponse<SliderCaptchaVerifyResponse> verifySliderCaptcha(@Valid @RequestBody SliderCaptchaVerifyRequest request) {
        return ApiResponse.success(
                "captcha verified",
                userAccountService.verifySliderCaptcha(
                        request.getChallengeId(),
                        request.getSliderX(),
                        request.getDurationMs(),
                        request.getTracks()
                )
        );
    }

    /**
     * 解析客户端 IP。
     *
     * <p>本地直连后端时使用 remoteAddr；经过 Gateway、Nginx 或后续 K8s Ingress 时优先使用
     * X-Forwarded-For / X-Real-IP。只取第一个 IP，避免多级代理列表影响 username + clientIp 风险 key 的稳定性。</p>
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        String remoteAddr = request.getRemoteAddr();
        return remoteAddr == null || remoteAddr.isBlank() ? "unknown" : remoteAddr;
    }
}
