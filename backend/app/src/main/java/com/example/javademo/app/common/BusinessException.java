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

    /** 返回给前端的业务错误码，当前阶段与 HTTP 状态码保持一致，便于学习和排查。 */
    private final int code;

    /** 实际 HTTP 响应状态，例如 400、401、404、409。 */
    private final HttpStatus status;

    public BusinessException(int code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
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

    public int getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
