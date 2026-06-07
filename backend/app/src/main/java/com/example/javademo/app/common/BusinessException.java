package com.example.javademo.app.common;

import org.springframework.http.HttpStatus;

/**
 * 业务异常基类。
 *
 * <p>Controller 和 Service 层遇到可预期的业务失败时抛出该异常，例如用户名重复、
 * 未登录、用户不存在等。全局异常处理器会把它转换成统一的 ApiResponse 结构，
 * 避免业务层到处手写 ResponseEntity。</p>
 */
public class BusinessException extends RuntimeException {

    /** 登录风险验证码必需：保留数字 code，方便现有前端请求层兼容。 */
    public static final int CODE_CAPTCHA_REQUIRED = 4601;

    /** 验证码错误或过期：用于区分普通参数错误和登录二次验证失败。 */
    public static final int CODE_CAPTCHA_INVALID = 4602;

    /** 返回给前端的业务错误码，当前阶段与 HTTP 状态码保持一致，便于学习和排查。 */
    private final int code;

    /** 实际 HTTP 响应状态，例如 400、401、404、409。 */
    private final HttpStatus status;

    /** 失败响应的非敏感业务上下文，例如 captchaRequired=true。 */
    private final Object data;

    public BusinessException(int code, String message, HttpStatus status) {
        this(code, message, status, null);
    }

    public BusinessException(int code, String message, HttpStatus status, Object data) {
        super(message);
        this.code = code;
        this.status = status;
        this.data = data;
    }

    /** 创建 400 Bad Request，表示请求参数或业务前置条件不满足。 */
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message, HttpStatus.BAD_REQUEST);
    }

    /** 创建 401 Unauthorized，表示未认证、token 无效或账号密码错误。 */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, message, HttpStatus.UNAUTHORIZED);
    }

    /** 创建 404 Not Found，表示当前请求依赖的资源不存在。 */
    public static BusinessException notFound(String message) {
        return new BusinessException(404, message, HttpStatus.NOT_FOUND);
    }

    /** 创建 409 Conflict，表示资源冲突，例如用户名已存在。 */
    public static BusinessException conflict(String message) {
        return new BusinessException(409, message, HttpStatus.CONFLICT);
    }

    /** 创建 502 Bad Gateway，表示对象存储等外部依赖当前不可用。 */
    public static BusinessException storageUnavailable(String message) {
        return new BusinessException(502, message, HttpStatus.BAD_GATEWAY);
    }

    /**
     * 创建登录验证码必需错误。
     *
     * <p>HTTP 状态仍使用 401，表达“当前登录认证条件不足”；业务 code 使用 4601，
     * 让 React/Vue 可以稳定地区分普通账号密码错误和需要展示滑块验证码的场景。</p>
     */
    public static BusinessException captchaRequired(String message, Object data) {
        return new BusinessException(CODE_CAPTCHA_REQUIRED, message, HttpStatus.UNAUTHORIZED, data);
    }

    /** 创建验证码校验失败错误，常用于 challenge 过期、滑动位置错误或 token 无效。 */
    public static BusinessException captchaInvalid(String message) {
        return new BusinessException(CODE_CAPTCHA_INVALID, message, HttpStatus.BAD_REQUEST);
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Object getData() {
        return data;
    }
}
