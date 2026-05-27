package com.example.javademo.notification.common;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 通知服务统一响应结构。
 *
 * <p>字段保持与 java-demo-app 一致，便于 React、Vue 和后续 Gateway 聚合时用同一套解析逻辑处理
 * 用户、任务和通知服务的响应。</p>
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

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(0, message, data);
    }

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
