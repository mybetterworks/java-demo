package com.example.javademo.task.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 任务服务全局异常处理器。
 *
 * <p>这里会记录必要的排查日志，但不会打印密码、完整 JWT、Authorization header 或请求体。
 * v0.5.2 已将这些日志写入控制台和文件，方便排查任务业务、服务间调用和异常链路。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        // 业务异常是可预期失败，例如负责人不存在或任务状态不合法，只记录摘要和错误码。
        log.warn("Task service business exception, code={}, message={}", exception.getCode(), exception.getMessage());
        return ResponseEntity
                .status(exception.getStatus())
                .body(ApiResponse.fail(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError == null ? "Invalid request" : fieldError.getField() + " " + fieldError.getDefaultMessage();
        // 参数校验失败不打印完整请求体，避免任务描述等用户输入长文本污染日志。
        log.warn("Task request validation failed, message={}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(400, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        // 未预期异常保留堆栈，便于本地定位；返回给前端的仍是统一错误。
        log.error("Unexpected task service exception", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(500, "Internal server error"));
    }
}
