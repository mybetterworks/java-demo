package com.example.javademo.gateway.common;

/**
 * 网关层统一响应结构。
 *
 * <p>Gateway 在过滤器阶段拦截请求时，请求还没有被转发到后端 Controller，
 * 因此不能复用后端的全局异常处理器。这里保持与 backend/app 的 ApiResponse 字段一致，
 * 让前端无论收到后端错误还是网关错误，都可以按 code、message、data 统一解析。</p>
 *
 * @param <T> data 字段承载的数据类型
 */
public class GatewayApiResponse<T> {

    private final int code;
    private final String message;
    private final T data;

    private GatewayApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 构造失败响应，当前主要用于网关 JWT 校验失败时返回 401。
     */
    public static GatewayApiResponse<Void> fail(int code, String message) {
        return new GatewayApiResponse<>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
