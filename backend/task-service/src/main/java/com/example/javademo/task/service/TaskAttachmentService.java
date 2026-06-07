package com.example.javademo.task.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.javademo.task.cache.TaskCacheService;
import com.example.javademo.task.common.BusinessException;
import com.example.javademo.task.dto.TaskAttachmentResponse;
import com.example.javademo.task.entity.TaskAttachment;
import com.example.javademo.task.entity.TaskItem;
import com.example.javademo.task.mapper.TaskAttachmentMapper;
import com.example.javademo.task.mapper.TaskMapper;
import com.example.javademo.task.security.AuthUser;
import com.example.javademo.task.storage.MinioProperties;
import com.example.javademo.task.storage.ObjectStorageService;
import com.example.javademo.task.storage.StoredObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 任务附件业务服务。
 *
 * <p>附件上传由 task-service 自己处理，因为附件元数据和任务详情属于任务业务边界。
 * 文件内容写入 MinIO，元数据写入 task_attachment；这条边界不会影响任务到用户服务的 Dubbo 校验，
 * 也不会改变任务到通知服务的 Feign 通知链路。</p>
 */
@Service
public class TaskAttachmentService {

    private static final Logger log = LoggerFactory.getLogger(TaskAttachmentService.class);

    private static final int NOT_DELETED = 0;

    /** v0.9 只放行常见协作附件类型，避免任意二进制直接进入对象存储。 */
    private static final Map<String, String> ALLOWED_ATTACHMENT_TYPES = Map.of(
            "image/png", "png",
            "image/jpeg", "jpg",
            "image/webp", "webp",
            "application/pdf", "pdf",
            "text/plain", "txt",
            "application/zip", "zip"
    );

    private final TaskMapper taskMapper;
    private final TaskAttachmentMapper taskAttachmentMapper;
    private final ObjectStorageService objectStorageService;
    private final MinioProperties minioProperties;
    private final TaskCacheService taskCacheService;

    public TaskAttachmentService(TaskMapper taskMapper, TaskAttachmentMapper taskAttachmentMapper, ObjectStorageService objectStorageService, MinioProperties minioProperties, TaskCacheService taskCacheService) {
        this.taskMapper = taskMapper;
        this.taskAttachmentMapper = taskAttachmentMapper;
        this.objectStorageService = objectStorageService;
        this.minioProperties = minioProperties;
        this.taskCacheService = taskCacheService;
    }

    /**
     * 上传任务附件。
     *
     * <p>先校验任务存在，再校验文件，最后写 MinIO 和附件元数据。
     * 这里没有把附件上传做成任务更新接口的一部分，是为了让前端可以在任务详情里独立追加附件。</p>
     */
    @Transactional
    public TaskAttachmentResponse uploadAttachment(Long taskId, MultipartFile file, AuthUser currentUser) {
        TaskItem task = getExistingTask(taskId);
        String contentType = normalizeAttachmentContentType(file);
        String safeFilename = sanitizeFilename(file.getOriginalFilename(), contentType);
        String objectKey = "tasks/task-" + task.getId() + "/" + UUID.randomUUID() + "-" + safeFilename;

        try {
            objectStorageService.putObject(
                    minioProperties.getTaskAttachmentBucket(),
                    objectKey,
                    file.getInputStream(),
                    file.getSize(),
                    contentType
            );
        } catch (IOException exception) {
            log.warn("Task attachment upload rejected, taskId={}, reason=input_stream_failed", task.getId());
            throw BusinessException.badRequest("Attachment file cannot be read");
        }

        LocalDateTime now = LocalDateTime.now();
        TaskAttachment attachment = new TaskAttachment();
        attachment.setTaskId(task.getId());
        attachment.setUploaderUserId(currentUser.getId());
        attachment.setOriginalFilename(safeFilename);
        attachment.setObjectKey(objectKey);
        attachment.setContentType(contentType);
        attachment.setFileSize(file.getSize());
        attachment.setDeleted(NOT_DELETED);
        attachment.setCreatedAt(now);
        attachment.setUpdatedAt(now);
        taskAttachmentMapper.insert(attachment);

        // 附件会展示在任务详情里，上传成功后要驱逐任务详情缓存并刷新列表版本，避免前端继续看到旧附件数量。
        taskCacheService.evictTask(task.getId(), "task_attachment_uploaded");
        taskCacheService.invalidateTaskLists("task_attachment_uploaded");
        log.info("Task attachment uploaded, taskId={}, attachmentId={}, uploaderUserId={}, bucket={}, objectKey={}, sizeBytes={}, contentType={}",
                task.getId(), attachment.getId(), currentUser.getId(), minioProperties.getTaskAttachmentBucket(), objectKey, file.getSize(), contentType);
        return TaskAttachmentResponse.from(attachment);
    }

    public List<TaskAttachmentResponse> listAttachments(Long taskId) {
        getExistingTask(taskId);
        return taskAttachmentMapper.selectList(Wrappers.<TaskAttachment>lambdaQuery()
                        .eq(TaskAttachment::getTaskId, taskId)
                        .orderByDesc(TaskAttachment::getCreatedAt)
                        .orderByDesc(TaskAttachment::getId))
                .stream()
                .map(TaskAttachmentResponse::from)
                .toList();
    }

    /**
     * 打开附件对象流。
     *
     * <p>下载前先确认附件属于当前任务，防止用户篡改 URL 中的 taskId 和 attachmentId 读取不匹配的对象。</p>
     */
    public AttachmentDownload openAttachment(Long taskId, Long attachmentId) {
        getExistingTask(taskId);
        TaskAttachment attachment = taskAttachmentMapper.selectOne(Wrappers.<TaskAttachment>lambdaQuery()
                .eq(TaskAttachment::getId, attachmentId)
                .eq(TaskAttachment::getTaskId, taskId)
                .last("LIMIT 1"));
        if (attachment == null) {
            throw BusinessException.notFound("Task attachment does not exist");
        }
        StoredObject storedObject = objectStorageService.getObject(minioProperties.getTaskAttachmentBucket(), attachment.getObjectKey());
        return new AttachmentDownload(attachment, storedObject);
    }

    private TaskItem getExistingTask(Long taskId) {
        if (taskId == null || taskId <= 0) {
            throw BusinessException.badRequest("Task id must be positive");
        }
        TaskItem task = taskMapper.selectById(taskId);
        if (task == null) {
            throw BusinessException.notFound("Task does not exist");
        }
        return task;
    }

    private String normalizeAttachmentContentType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("Attachment file must not be empty");
        }
        if (file.getSize() > minioProperties.getMaxTaskAttachmentSizeBytes()) {
            throw BusinessException.badRequest("Attachment file is too large");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().trim().toLowerCase();
        if (!ALLOWED_ATTACHMENT_TYPES.containsKey(contentType)) {
            throw BusinessException.badRequest("Attachment file type is not allowed");
        }
        return contentType;
    }

    /**
     * 清洗原始文件名。
     *
     * <p>浏览器上传的文件名可能包含路径分隔符或特殊字符，不能直接拼入 objectKey。
     * 这里保留常见中英文、数字、点、下划线和短横线，其他字符统一替换为下划线。</p>
     */
    private String sanitizeFilename(String originalFilename, String contentType) {
        String fallback = "attachment." + ALLOWED_ATTACHMENT_TYPES.get(contentType);
        String filename = originalFilename == null || originalFilename.isBlank() ? fallback : originalFilename.trim();
        filename = filename.replace("\\", "/");
        int lastSlash = filename.lastIndexOf('/');
        if (lastSlash >= 0) {
            filename = filename.substring(lastSlash + 1);
        }
        filename = filename.replaceAll("[^\\p{IsHan}a-zA-Z0-9._-]", "_");
        if (filename.isBlank() || ".".equals(filename) || "..".equals(filename)) {
            return fallback;
        }
        return filename.length() > 120 ? filename.substring(filename.length() - 120) : filename;
    }

    public record AttachmentDownload(TaskAttachment attachment, StoredObject storedObject) {
    }
}
