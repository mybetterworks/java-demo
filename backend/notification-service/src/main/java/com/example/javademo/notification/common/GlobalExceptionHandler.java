package com.example.javademo.notification.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 通知服务全局异常处理器。
 *
 * <p>这里统一输出错误响应并记录必要日志。日志只记录异常类型和摘要，不打印 Authorization、
 * JWT 或请求体，避免把敏感信息写入控制台。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        // 通知业务异常通常是通知不存在、非本人操作或参数不合法，记录摘要即可。
        log.warn("Notification service business exception, code={}, message={}", exception.getCode(), exception.getMessage());
        return ResponseEntity
                .status(exception.getStatus())
                .body(ApiResponse.fail(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError == null ? "Invalid request" : fieldError.getField() + " " + fieldError.getDefaultMessage();
        // 不打印完整通知请求体，避免通知正文或后续附件信息进入日志。
        log.warn("Notification request validation failed, message={}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(400, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        // 未预期异常需要堆栈帮助排查，但前端仍只接收稳定的统一错误响应。
        log.error("Unexpected notification service exception", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(500, "Internal server error"));
    }
}
