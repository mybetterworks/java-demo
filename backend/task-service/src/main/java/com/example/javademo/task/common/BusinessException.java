package com.example.javademo.task.common;

import org.springframework.http.HttpStatus;

/**
 * 任务服务业务异常。
 *
 * <p>Service 或下游客户端遇到可预期失败时抛出该异常，由全局异常处理器统一转换为 ApiResponse。
 * 这样 Controller 可以专注表达接口语义，而不是散落各种错误响应拼装逻辑。</p>
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

    public static BusinessException notFound(String message) {
        return new BusinessException(404, message, HttpStatus.NOT_FOUND);
    }

    public static BusinessException downstream(String message) {
        return new BusinessException(502, message, HttpStatus.BAD_GATEWAY);
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
