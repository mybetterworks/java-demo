package com.example.javademo.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登录需要滑块验证码时返回给前端的失败上下文。
 *
 * <p>该对象只包含前端展示和流程判断所需的非敏感信息，不包含验证码答案、验证码 token、
 * 密码、JWT 或任何可复用凭证。React/Vue 收到该对象后再调用验证码 challenge 接口。</p>
 */
@Schema(description = "登录需要验证码的失败上下文")
public class CaptchaRequiredResponse {

    /** 前端据此判断是否展示滑块验证码区域。 */
    @Schema(description = "是否需要滑块验证码", example = "true")
    private boolean captchaRequired;

    /** 当前失败次数，便于本地学习和前端提示，不作为安全凭证。 */
    @Schema(description = "当前 5 分钟窗口内失败次数", example = "3")
    private int failureCount;

    /** 失败阈值，当前 v0.5.4 固定为 3。 */
    @Schema(description = "触发验证码的失败阈值", example = "3")
    private int failureThreshold;

    /** 时间窗口秒数，当前 v0.5.4 固定为 300 秒。 */
    @Schema(description = "失败计数窗口秒数", example = "300")
    private long windowSeconds;

    public CaptchaRequiredResponse() {
    }

    public CaptchaRequiredResponse(boolean captchaRequired, int failureCount, int failureThreshold, long windowSeconds) {
        this.captchaRequired = captchaRequired;
        this.failureCount = failureCount;
        this.failureThreshold = failureThreshold;
        this.windowSeconds = windowSeconds;
    }

    public boolean isCaptchaRequired() {
        return captchaRequired;
    }

    public void setCaptchaRequired(boolean captchaRequired) {
        this.captchaRequired = captchaRequired;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public int getFailureThreshold() {
        return failureThreshold;
    }

    public void setFailureThreshold(int failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    public long getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(long windowSeconds) {
        this.windowSeconds = windowSeconds;
    }
}
