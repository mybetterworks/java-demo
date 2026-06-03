package com.example.javademo.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

/**
 * 拼图验证码拖动轨迹点。
 *
 * <p>前端会在拖动过程中收集若干基础轨迹点，后端只做轻量级合理性校验，
 * 用来区分真人拖拽和极简脚本调用，不承诺构成专业级风控。</p>
 */
@Schema(description = "拼图验证码拖动轨迹点")
public class CaptchaTrackPoint {

    /** 当前轨迹点的横坐标，单位像素。 */
    @Schema(description = "轨迹点横坐标", example = "118")
    @Min(0)
    private int x;

    /** 当前轨迹点的纵坐标，单位像素。 */
    @Schema(description = "轨迹点纵坐标", example = "82")
    @Min(0)
    private int y;

    /** 自拖动开始后的时间戳，单位毫秒。 */
    @Schema(description = "轨迹点时间戳，单位毫秒", example = "840")
    @Min(0)
    private long t;

    public CaptchaTrackPoint() {
    }

    public CaptchaTrackPoint(int x, int y, long t) {
        this.x = x;
        this.y = y;
        this.t = t;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public long getT() {
        return t;
    }

    public void setT(long t) {
        this.t = t;
    }
}
