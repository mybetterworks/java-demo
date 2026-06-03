package com.example.javademo.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 滑块验证码挑战响应。
 *
 * <p>v0.5.4 使用“滑动到终点”的学习型 MVP：后端仍然生成一次性 challengeId 并保存答案，
 * 前端只拿到绘制滑块所需的轨道宽度、滑块宽度和过期时间。生产级拼图验证码应在后续安全增强中替换。</p>
 */
@Schema(description = "滑块验证码挑战响应")
public class SliderCaptchaChallengeResponse {

    /** 一次性 challenge ID，用于 verify 接口定位验证码状态。 */
    @Schema(description = "验证码挑战 ID")
    private String challengeId;

    /** 滑块轨道宽度，前端据此渲染滑块。 */
    @Schema(description = "滑块轨道宽度", example = "320")
    private int trackWidth;

    /** 滑块本身宽度，前端据此计算可滑动最大位置。 */
    @Schema(description = "滑块宽度", example = "48")
    private int puzzleWidth;

    /** challenge 过期秒数，当前为 120 秒。 */
    @Schema(description = "验证码挑战过期秒数", example = "120")
    private long expiresInSeconds;

    /** 展示给用户的提示文案，不包含答案或 token。 */
    @Schema(description = "滑块提示文案")
    private String instruction;

    public SliderCaptchaChallengeResponse() {
    }

    /**
     * 构造器。
     * @param challengeId 一次性 challenge ID
     * @param trackWidth 滑块轨道宽度
     * @param puzzleWidth 滑块宽度
     * @param expiresInSeconds challenge 过期秒数
     * @param instruction 展示给用户的提示文案
     */
    public SliderCaptchaChallengeResponse(String challengeId, int trackWidth, int puzzleWidth, long expiresInSeconds, String instruction) {
        this.challengeId = challengeId;
        this.trackWidth = trackWidth;
        this.puzzleWidth = puzzleWidth;
        this.expiresInSeconds = expiresInSeconds;
        this.instruction = instruction;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public int getTrackWidth() {
        return trackWidth;
    }

    public void setTrackWidth(int trackWidth) {
        this.trackWidth = trackWidth;
    }

    public int getPuzzleWidth() {
        return puzzleWidth;
    }

    public void setPuzzleWidth(int puzzleWidth) {
        this.puzzleWidth = puzzleWidth;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }
}
