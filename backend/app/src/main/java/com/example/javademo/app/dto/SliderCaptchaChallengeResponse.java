package com.example.javademo.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 拼图验证码挑战响应。
 *
 * <p>v0.5.5 把原先“滑动到终点”的学习型验证码升级为“固定背景图 + 随机缺口拼图”。
 * 这里返回的只有图像数据和渲染参数，不包含任何可直接推导答案的字段。</p>
 */
@Schema(description = "拼图验证码挑战响应")
public class SliderCaptchaChallengeResponse {

    /** 一次性 challenge ID，用于 verify 接口定位验证码状态。 */
    @Schema(description = "验证码挑战 ID")
    private String challengeId;

    /** 固定背景图，使用 data URL 返回，前端可以直接渲染到 img 标签。 */
    @Schema(description = "背景图 data URL")
    private String backgroundImage;

    /** 拼图块图像，使用 data URL 返回，前端可以直接渲染到 img 标签。 */
    @Schema(description = "拼图块 data URL")
    private String puzzleImage;

    /** 拼图轨道宽度，前端据此限制拖动范围。 */
    @Schema(description = "拼图轨道宽度", example = "320")
    private int trackWidth;

    /** 背景图宽度，前端据此缩放渲染。 */
    @Schema(description = "背景图宽度", example = "320")
    private int imageWidth;

    /** 背景图高度，前端据此缩放渲染。 */
    @Schema(description = "背景图高度", example = "160")
    private int imageHeight;

    /** 拼图块宽度，前端据此计算可拖动最大位置。 */
    @Schema(description = "拼图块宽度", example = "48")
    private int puzzleWidth;

    /** 拼图块高度。 */
    @Schema(description = "拼图块高度", example = "48")
    private int puzzleHeight;

    /** 拼图块所在的垂直位置，仅用于前端摆放拼图块，不代表答案。 */
    @Schema(description = "拼图块垂直位置", example = "72")
    private int puzzleY;

    /** challenge 过期秒数，当前为 120 秒。 */
    @Schema(description = "验证码挑战过期秒数", example = "120")
    private long expiresInSeconds;

    /** 展示给用户的提示文案，不包含答案或 token。 */
    @Schema(description = "拼图验证码提示文案")
    private String instruction;

    public SliderCaptchaChallengeResponse() {
    }

    /**
     * 构造器。
     *
     * @param challengeId 一次性 challenge ID
     * @param backgroundImage 背景图 data URL
     * @param puzzleImage 拼图块 data URL
     * @param trackWidth 拼图轨道宽度
     * @param imageWidth 背景图宽度
     * @param imageHeight 背景图高度
     * @param puzzleWidth 拼图块宽度
     * @param puzzleHeight 拼图块高度
     * @param puzzleY 拼图块垂直位置
     * @param expiresInSeconds challenge 过期秒数
     * @param instruction 展示给用户的提示文案
     */
    public SliderCaptchaChallengeResponse(
            String challengeId,
            String backgroundImage,
            String puzzleImage,
            int trackWidth,
            int imageWidth,
            int imageHeight,
            int puzzleWidth,
            int puzzleHeight,
            int puzzleY,
            long expiresInSeconds,
            String instruction
    ) {
        this.challengeId = challengeId;
        this.backgroundImage = backgroundImage;
        this.puzzleImage = puzzleImage;
        this.trackWidth = trackWidth;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.puzzleWidth = puzzleWidth;
        this.puzzleHeight = puzzleHeight;
        this.puzzleY = puzzleY;
        this.expiresInSeconds = expiresInSeconds;
        this.instruction = instruction;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public String getPuzzleImage() {
        return puzzleImage;
    }

    public void setPuzzleImage(String puzzleImage) {
        this.puzzleImage = puzzleImage;
    }

    public int getTrackWidth() {
        return trackWidth;
    }

    public void setTrackWidth(int trackWidth) {
        this.trackWidth = trackWidth;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public int getPuzzleWidth() {
        return puzzleWidth;
    }

    public void setPuzzleWidth(int puzzleWidth) {
        this.puzzleWidth = puzzleWidth;
    }

    public int getPuzzleHeight() {
        return puzzleHeight;
    }

    public void setPuzzleHeight(int puzzleHeight) {
        this.puzzleHeight = puzzleHeight;
    }

    public int getPuzzleY() {
        return puzzleY;
    }

    public void setPuzzleY(int puzzleY) {
        this.puzzleY = puzzleY;
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
