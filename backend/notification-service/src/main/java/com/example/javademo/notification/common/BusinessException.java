package com.example.javademo.notification.common;

import org.springframework.http.HttpStatus;

/**
 * 通知服务业务异常。
 *
 * <p>服务层遇到可预期失败时抛出该异常，由 GlobalExceptionHandler 统一转换为 ApiResponse，
 * 避免 Controller 中散落大量 ResponseEntity 拼装代码。</p>
 */
public class BusinessException extends RuntimeException {

    private final int code;
    private final HttpStatus status;

    public BusinessException(int code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message, HttpStatus.BAD_REQUEST);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, message, HttpStatus.UNAUTHORIZED);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message, HttpStatus.FORBIDDEN);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(404, message, HttpStatus.NOT_FOUND);
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
