package com.example.javademo.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 滑块验证码校验请求。
 *
 * <p>前端把 challengeId 和用户拖动后的最终位置提交给后端。后端只返回一次性 captchaToken，
 * 不返回答案，也不会在日志中打印该 token。</p>
 */
@Schema(description = "滑块验证码校验请求")
public class SliderCaptchaVerifyRequest {

    /** challenge 接口返回的一次性验证码挑战 ID。 */
    @Schema(description = "验证码挑战 ID")
    @NotBlank
    @Size(max = 64)
    private String challengeId;

    /** 用户最终拖动的位置，单位为像素。 */
    @Schema(description = "用户拖动后的滑块位置", example = "272")
    @Min(0)
    private int sliderPosition;

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public int getSliderPosition() {
        return sliderPosition;
    }

    public void setSliderPosition(int sliderPosition) {
        this.sliderPosition = sliderPosition;
    }
}
