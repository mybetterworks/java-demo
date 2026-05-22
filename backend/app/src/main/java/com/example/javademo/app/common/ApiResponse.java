package com.example.javademo.app.common;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 后端统一响应结构。
 *
 * <p>所有 Controller 都返回该结构，前端和接口测试只需要固定解析 code、message、data 三个字段。
 * 这样后续即使业务接口继续增加，也能保持统一的成功和失败响应格式。</p>
 *
 * @param <T> data 字段承载的真实业务数据类型
 */
@Schema(description = "统一 API 响应")
public class ApiResponse<T> {

    @Schema(description = "业务状态码，0 表示成功", example = "0")
    private int code;

    @Schema(description = "响应消息", example = "success")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    public ApiResponse() {
    }

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 构造默认成功响应，message 固定为 success。
     *
     * @param data 业务数据
     * @param <T> 业务数据类型
     * @return 统一成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    /**
     * 构造可自定义 message 的成功响应，适合注册、登录这类需要更明确语义的场景。
     *
     * @param message 成功消息
     * @param data 业务数据
     * @param <T> 业务数据类型
     * @return 统一成功响应
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(0, message, data);
    }

    /**
     * 构造失败响应，通常由全局异常处理器或认证拦截器调用。
     *
     * @param code 业务错误码，当前 v0.1 直接复用常见 HTTP 状态码
     * @param message 错误说明
     * @return 统一失败响应
     */
    public static ApiResponse<Void> fail(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
