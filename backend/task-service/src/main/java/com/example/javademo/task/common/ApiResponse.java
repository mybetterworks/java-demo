package com.example.javademo.task.common;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 任务服务统一响应结构。
 *
 * <p>该结构和 java-demo-app、notification-service 保持一致，让前端、Gateway 和接口测试不需要
 * 按服务区分成功或失败响应格式。</p>
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
