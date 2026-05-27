package com.example.javademo.task.controller;

import com.example.javademo.task.common.ApiResponse;
import com.example.javademo.task.config.OpenApiConfig;
import com.example.javademo.task.dto.CreateTaskRequest;
import com.example.javademo.task.dto.PageResponse;
import com.example.javademo.task.dto.TaskResponse;
import com.example.javademo.task.dto.UpdateTaskRequest;
import com.example.javademo.task.dto.UpdateTaskStatusRequest;
import com.example.javademo.task.security.CurrentUserContext;
import com.example.javademo.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务接口。
 *
 * <p>所有业务接口都需要 JWT。当前版本只做登录用户级别的最小鉴权，真正的角色权限和数据权限留到后续独立版本。</p>
 */
@Tag(name = "Tasks", description = "任务管理接口")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "创建任务", description = "当前登录用户创建任务，可指定负责人；创建成功后会生成任务通知。")
    @PostMapping
    public ApiResponse<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return ApiResponse.success("created", taskService.createTask(request, CurrentUserContext.getRequired()));
    }

    @Operation(summary = "我的任务", description = "查询当前登录用户创建或负责的任务。")
    @GetMapping("/my")
    public ApiResponse<PageResponse<TaskResponse>> myTasks(
            @Parameter(description = "当前页码，从 1 开始") @RequestParam(name = "current", defaultValue = "1") Long current,
            @Parameter(description = "每页条数，最大 100") @RequestParam(name = "size", defaultValue = "10") Long size,
            @Parameter(description = "任务状态，可选") @RequestParam(name = "status", required = false) String status) {
        return ApiResponse.success(taskService.pageMyTasks(CurrentUserContext.getRequired(), current, size, status));
    }

    @Operation(summary = "任务分页查询", description = "分页查询任务，可按状态和负责人筛选。")
    @GetMapping
    public ApiResponse<PageResponse<TaskResponse>> pageTasks(
            @Parameter(description = "当前页码，从 1 开始") @RequestParam(name = "current", defaultValue = "1") Long current,
            @Parameter(description = "每页条数，最大 100") @RequestParam(name = "size", defaultValue = "10") Long size,
            @Parameter(description = "任务状态，可选") @RequestParam(name = "status", required = false) String status,
            @Parameter(description = "负责人用户 ID，可选") @RequestParam(name = "assigneeUserId", required = false) Long assigneeUserId) {
        return ApiResponse.success(taskService.pageTasks(current, size, status, assigneeUserId));
    }

    @Operation(summary = "任务详情", description = "根据任务 ID 查询任务详情。")
    @GetMapping("/{id:\\d+}")
    public ApiResponse<TaskResponse> getTask(@PathVariable("id") Long id) {
        return ApiResponse.success(taskService.getTask(id));
    }

    @Operation(summary = "更新任务", description = "更新标题、描述、负责人、优先级和截止时间。负责人变化时会重新生成通知。")
    @PutMapping("/{id:\\d+}")
    public ApiResponse<TaskResponse> updateTask(@PathVariable("id") Long id, @Valid @RequestBody UpdateTaskRequest request) {
        return ApiResponse.success("updated", taskService.updateTask(id, request, CurrentUserContext.getRequired()));
    }

    @Operation(summary = "更新任务状态", description = "修改任务状态，并通知负责人。")
    @PutMapping("/{id:\\d+}/status")
    public ApiResponse<TaskResponse> updateStatus(@PathVariable("id") Long id, @Valid @RequestBody UpdateTaskStatusRequest request) {
        return ApiResponse.success("status updated", taskService.updateStatus(id, request, CurrentUserContext.getRequired()));
    }

    @Operation(summary = "删除任务", description = "逻辑删除任务，保留数据库记录用于后续审计和搜索实验。")
    @DeleteMapping("/{id:\\d+}")
    public ApiResponse<Void> deleteTask(@PathVariable("id") Long id) {
        taskService.deleteTask(id, CurrentUserContext.getRequired());
        return ApiResponse.success("deleted", null);
    }
}
