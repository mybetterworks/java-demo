package com.example.javademo.task.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.javademo.task.client.NotificationServiceClient;
import com.example.javademo.task.client.UserServiceClient;
import com.example.javademo.task.common.BusinessException;
import com.example.javademo.task.dto.CreateTaskRequest;
import com.example.javademo.task.dto.PageResponse;
import com.example.javademo.task.dto.TaskResponse;
import com.example.javademo.task.dto.UpdateTaskRequest;
import com.example.javademo.task.dto.UpdateTaskStatusRequest;
import com.example.javademo.task.entity.TaskItem;
import com.example.javademo.task.mapper.TaskMapper;
import com.example.javademo.task.security.AuthUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 任务业务服务。
 *
 * <p>本服务只管理任务数据本身。用户是否存在通过 UserServiceClient 校验，通知写入通过
 * NotificationServiceClient 完成，保持 v0.5.1 的微服务边界清晰可见。</p>
 */
@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private static final String STATUS_TODO = "TODO";
    private static final String PRIORITY_MEDIUM = "MEDIUM";
    private static final int NOT_DELETED = 0;
    private static final long MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_STATUSES = Set.of("TODO", "IN_PROGRESS", "DONE", "CANCELLED");
    private static final Set<String> ALLOWED_PRIORITIES = Set.of("LOW", "MEDIUM", "HIGH");

    private final TaskMapper taskMapper;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public TaskService(TaskMapper taskMapper, UserServiceClient userServiceClient, NotificationServiceClient notificationServiceClient) {
        this.taskMapper = taskMapper;
        this.userServiceClient = userServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    /**
     * 创建任务。
     *
     * <p>创建时会先校验负责人存在，再写入 task_item，最后同步创建任务通知。这里故意保留同步链路，
     * 方便后续对比 MQ 异步化和 Seata 分布式事务的价值。</p>
     */
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, AuthUser currentUser) {
        Long assigneeUserId = request.getAssigneeUserId() == null ? currentUser.getId() : request.getAssigneeUserId();
        validatePositiveUserId(assigneeUserId);
        userServiceClient.requireUser(assigneeUserId, currentUser);

        LocalDateTime now = LocalDateTime.now();
        TaskItem task = new TaskItem();
        task.setTitle(requireNonBlank(request.getTitle(), "Task title must not be blank"));
        task.setDescription(trimToNull(request.getDescription()));
        task.setCreatorUserId(currentUser.getId());
        task.setAssigneeUserId(assigneeUserId);
        task.setStatus(STATUS_TODO);
        task.setPriority(resolvePriority(request.getPriority(), PRIORITY_MEDIUM));
        task.setDueTime(request.getDueTime());
        task.setDeleted(NOT_DELETED);
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        taskMapper.insert(task);

        notificationServiceClient.createTaskNotification(
                assigneeUserId,
                "你有一个新任务",
                "任务「" + task.getTitle() + "」已分配给你，请及时处理。",
                task,
                currentUser
        );
        log.info("Task created, taskId={}, creatorUserId={}, assigneeUserId={}",
                task.getId(), task.getCreatorUserId(), task.getAssigneeUserId());
        return TaskResponse.from(task);
    }

    /**
     * 查询当前用户创建或负责的任务。
     */
    public PageResponse<TaskResponse> pageMyTasks(AuthUser currentUser, Long current, Long size, String status) {
        LambdaQueryWrapper<TaskItem> queryWrapper = Wrappers.<TaskItem>lambdaQuery()
                .and(wrapper -> wrapper
                        .eq(TaskItem::getCreatorUserId, currentUser.getId())
                        .or()
                        .eq(TaskItem::getAssigneeUserId, currentUser.getId()));
        if (status != null && !status.isBlank()) {
            queryWrapper.eq(TaskItem::getStatus, resolveStatus(status));
        }
        queryWrapper.orderByDesc(TaskItem::getUpdatedAt)
                .orderByDesc(TaskItem::getId);
        return page(queryWrapper, current, size);
    }

    /**
     * 管理端分页查询任务。
     *
     * <p>当前还没有复杂 RBAC，沿用项目早期“登录即可访问”的简化策略；真正权限模型会在后续 milestone 引入。</p>
     */
    public PageResponse<TaskResponse> pageTasks(Long current, Long size, String status, Long assigneeUserId) {
        LambdaQueryWrapper<TaskItem> queryWrapper = Wrappers.<TaskItem>lambdaQuery()
                .eq(assigneeUserId != null, TaskItem::getAssigneeUserId, assigneeUserId);
        if (status != null && !status.isBlank()) {
            queryWrapper.eq(TaskItem::getStatus, resolveStatus(status));
        }
        queryWrapper.orderByDesc(TaskItem::getUpdatedAt)
                .orderByDesc(TaskItem::getId);
        return page(queryWrapper, current, size);
    }

    /**
     * 查询任务详情。
     */
    public TaskResponse getTask(Long id) {
        return TaskResponse.from(getExistingTask(id));
    }

    /**
     * 更新任务基础字段。
     */
    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest request, AuthUser currentUser) {
        TaskItem task = getExistingTask(id);
        Long oldAssigneeUserId = task.getAssigneeUserId();

        if (request.getTitle() != null) {
            task.setTitle(requireNonBlank(request.getTitle(), "Task title must not be blank"));
        }
        if (request.getDescription() != null) {
            task.setDescription(trimToNull(request.getDescription()));
        }
        if (request.getAssigneeUserId() != null) {
            validatePositiveUserId(request.getAssigneeUserId());
            userServiceClient.requireUser(request.getAssigneeUserId(), currentUser);
            task.setAssigneeUserId(request.getAssigneeUserId());
        }
        if (request.getPriority() != null) {
            task.setPriority(resolvePriority(request.getPriority(), task.getPriority()));
        }
        if (request.getDueTime() != null) {
            task.setDueTime(request.getDueTime());
        }
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        if (!oldAssigneeUserId.equals(task.getAssigneeUserId())) {
            notificationServiceClient.createTaskNotification(
                    task.getAssigneeUserId(),
                    "任务负责人已变更",
                    "任务「" + task.getTitle() + "」已分配给你。",
                    task,
                    currentUser
            );
        }
        log.info("Task updated, taskId={}, operatorUserId={}, assigneeChanged={}",
                task.getId(), currentUser.getId(), !oldAssigneeUserId.equals(task.getAssigneeUserId()));
        return TaskResponse.from(task);
    }

    /**
     * 更新任务状态。
     */
    @Transactional
    public TaskResponse updateStatus(Long id, UpdateTaskStatusRequest request, AuthUser currentUser) {
        TaskItem task = getExistingTask(id);
        String nextStatus = resolveStatus(request.getStatus());
        if (!nextStatus.equals(task.getStatus())) {
            task.setStatus(nextStatus);
            task.setUpdatedAt(LocalDateTime.now());
            taskMapper.updateById(task);
            notificationServiceClient.createTaskNotification(
                    task.getAssigneeUserId(),
                    "任务状态已更新",
                    "任务「" + task.getTitle() + "」状态已更新为 " + nextStatus + "。",
                    task,
                    currentUser
            );
            log.info("Task status updated, taskId={}, status={}, operatorUserId={}",
                    task.getId(), nextStatus, currentUser.getId());
        }
        return TaskResponse.from(task);
    }

    /**
     * 逻辑删除任务。
     */
    @Transactional
    public void deleteTask(Long id, AuthUser currentUser) {
        TaskItem task = getExistingTask(id);
        taskMapper.deleteById(task.getId());
        log.info("Task deleted logically, taskId={}, operatorUserId={}", task.getId(), currentUser.getId());
    }

    private PageResponse<TaskResponse> page(LambdaQueryWrapper<TaskItem> queryWrapper, Long current, Long size) {
        IPage<TaskItem> page = taskMapper.selectPage(new Page<>(normalizeCurrent(current), normalizeSize(size)), queryWrapper);
        List<TaskResponse> records = page.getRecords().stream()
                .map(TaskResponse::from)
                .toList();
        return new PageResponse<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getPages(), records);
    }

    private TaskItem getExistingTask(Long id) {
        if (id == null || id <= 0) {
            throw BusinessException.badRequest("Task id must be positive");
        }
        TaskItem task = taskMapper.selectById(id);
        if (task == null) {
            throw BusinessException.notFound("Task does not exist");
        }
        return task;
    }

    private void validatePositiveUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw BusinessException.badRequest("Assignee user id must be positive");
        }
    }

    private String resolveStatus(String status) {
        String normalized = requireNonBlank(status, "Task status must not be blank").toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw BusinessException.badRequest("Task status must be TODO, IN_PROGRESS, DONE or CANCELLED");
        }
        return normalized;
    }

    private String resolvePriority(String priority, String defaultValue) {
        if (priority == null || priority.isBlank()) {
            return defaultValue;
        }
        String normalized = priority.trim().toUpperCase();
        if (!ALLOWED_PRIORITIES.contains(normalized)) {
            throw BusinessException.badRequest("Task priority must be LOW, MEDIUM or HIGH");
        }
        return normalized;
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw BusinessException.badRequest(message);
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private long normalizeCurrent(Long current) {
        if (current == null || current < 1) {
            return 1;
        }
        return current;
    }

    private long normalizeSize(Long size) {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
