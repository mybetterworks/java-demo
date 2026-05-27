package com.example.javademo.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 更新任务状态请求。
 */
@Schema(description = "更新任务状态请求")
public class UpdateTaskStatusRequest {

    @NotBlank
    @Size(max = 32)
    @Schema(description = "任务状态：TODO、IN_PROGRESS、DONE、CANCELLED", example = "IN_PROGRESS")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
