package com.example.javademo.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 拼图验证码校验请求。
 *
 * <p>前端会把 challengeId、最终停靠位置 sliderX、拖动耗时 durationMs 和基础轨迹一并提交给后端。
 * 后端只返回一次性 captchaToken，不返回答案，也不会在日志中打印该 token。</p>
 */
@Schema(description = "拼图验证码校验请求")
public class SliderCaptchaVerifyRequest {

    /** challenge 接口返回的一次性验证码挑战 ID。 */
    @Schema(description = "验证码挑战 ID")
    @NotBlank
    @Size(max = 64)
    private String challengeId;

    /** 用户最终拖动到的横坐标，单位为像素。兼容旧字段 sliderPosition。 */
    @Schema(description = "用户最终拖动到的横坐标", example = "132")
    @JsonAlias("sliderPosition")
    @Min(0)
    private int sliderX;

    /** 本次拖动持续的时间，单位毫秒。 */
    @Schema(description = "拖动持续时间，单位毫秒", example = "1460")
    @Min(0)
    private long durationMs;

    /** 基础拖动轨迹点，后端会校验其合理性。 */
    @Schema(description = "基础拖动轨迹点")
    @Valid
    private List<CaptchaTrackPoint> tracks;

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public int getSliderX() {
        return sliderX;
    }

    public void setSliderX(int sliderX) {
        this.sliderX = sliderX;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public List<CaptchaTrackPoint> getTracks() {
        return tracks;
    }

    public void setTracks(List<CaptchaTrackPoint> tracks) {
        this.tracks = tracks;
    }
}
