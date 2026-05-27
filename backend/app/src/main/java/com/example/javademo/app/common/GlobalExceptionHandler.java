package com.example.javademo.app.common;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * <p>这里把不同来源的异常统一转换成 ApiResponse，确保前端看到的错误结构稳定。
 * v0.1 只保留最小错误模型；后续可以在这里继续扩展错误码、链路 ID、字段级错误列表等能力。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 记录未预期异常，既不暴露给前端，也方便本地调试和后续接入日志系统。 */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务层主动抛出的可预期异常，例如用户名重复、未认证、用户不存在。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        // 业务异常属于可预期错误，记录 code 和摘要即可，不输出请求体或敏感字段。
        log.warn("Application business exception, code={}, status={}, message={}",
                exception.getCode(), exception.getStatus().value(), exception.getMessage());
        return ResponseEntity
                .status(exception.getStatus())
                .body(ApiResponse.fail(exception.getCode(), exception.getMessage()));
    }

    /**
     * 处理 Bean Validation 参数校验失败。
     *
     * <p>例如注册时用户名为空或密码长度不足，会进入该方法并返回 400。
     * 当前只取第一个字段错误，保持响应简单；后续表单复杂后可扩展为字段错误数组。</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError == null ? "Invalid request" : fieldError.getField() + " " + fieldError.getDefaultMessage();
        // 参数校验失败只记录字段和摘要，不打印完整请求体，避免密码等表单内容进入日志。
        log.warn("Application request validation failed, message={}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(400, message));
    }

    /**
     * 处理数据库唯一索引冲突。
     *
     * <p>注册接口在 Service 层已经先查重，但并发注册时仍可能同时通过查询；
     * 数据库唯一索引是最后一道防线，这里把底层异常转换为友好的 409。</p>
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateKeyException(DuplicateKeyException exception) {
        // 唯一索引冲突通常由并发注册或创建用户触发，记录异常类型即可。
        log.warn("Application duplicate key conflict, reason={}", exception.getClass().getSimpleName());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(409, "Username already exists"));
    }

    /**
     * 兜底处理未预料到的系统异常。
     *
     * <p>生产环境不应直接把异常堆栈暴露给前端，所以这里只返回通用错误信息。
     * 后续接入日志系统或链路追踪后，可以在服务端日志中记录详细异常。</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        log.error("Unexpected application exception", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(500, "Internal server error"));
    }
}
