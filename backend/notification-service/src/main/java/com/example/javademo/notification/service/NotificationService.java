package com.example.javademo.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.javademo.notification.common.BusinessException;
import com.example.javademo.notification.dto.CreateNotificationRequest;
import com.example.javademo.notification.dto.NotificationResponse;
import com.example.javademo.notification.dto.PageResponse;
import com.example.javademo.notification.entity.NotificationMessage;
import com.example.javademo.notification.mapper.NotificationMapper;
import com.example.javademo.notification.security.AuthUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知业务服务。
 *
 * <p>本服务只负责通知自身的创建、查询和已读状态，不在这里查询用户详情。用户是否存在由调用方
 * task-service 在创建任务时校验，通知服务只保存接收人 ID，保持服务边界简单清晰。</p>
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private static final int UNREAD = 0;
    private static final int READ = 1;
    private static final long MAX_PAGE_SIZE = 100;

    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    /**
     * 创建通知。
     *
     * <p>创建通知通常由 task-service 调用。日志记录接收人和业务 ID，不记录通知正文，避免把未来可能包含
     * 用户输入的长文本写入日志。</p>
     */
    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request, AuthUser currentUser) {
        if (request.getReceiverUserId() == null || request.getReceiverUserId() <= 0) {
            throw BusinessException.badRequest("Receiver user id must be positive");
        }

        LocalDateTime now = LocalDateTime.now();
        NotificationMessage message = new NotificationMessage();
        message.setReceiverUserId(request.getReceiverUserId());
        message.setTitle(request.getTitle().trim());
        message.setContent(request.getContent().trim());
        message.setType(resolveType(request.getType()));
        message.setBizType(resolveBizType(request.getBizType()));
        message.setBizId(request.getBizId());
        message.setReadStatus(UNREAD);
        message.setCreatedAt(now);
        notificationMapper.insert(message);

        // 通知正文可能来自用户输入，日志只记录可定位链路的 ID 和业务类型。
        log.info("Notification created, notificationId={}, receiverUserId={}, bizType={}, bizId={}, operatorUserId={}",
                message.getId(), message.getReceiverUserId(), message.getBizType(), message.getBizId(), currentUser.getId());
        return NotificationResponse.from(message);
    }

    /**
     * 分页查询当前用户收到的通知。
     */
    public PageResponse<NotificationResponse> pageMyNotifications(AuthUser currentUser, Long current, Long size, Integer readStatus) {
        Integer normalizedReadStatus = readStatus == null ? null : normalizeReadStatus(readStatus);
        LambdaQueryWrapper<NotificationMessage> queryWrapper = Wrappers.<NotificationMessage>lambdaQuery()
                .eq(NotificationMessage::getReceiverUserId, currentUser.getId())
                .eq(normalizedReadStatus != null, NotificationMessage::getReadStatus, normalizedReadStatus)
                .orderByAsc(NotificationMessage::getReadStatus)
                .orderByDesc(NotificationMessage::getCreatedAt)
                .orderByDesc(NotificationMessage::getId);

        IPage<NotificationMessage> page = notificationMapper.selectPage(new Page<>(normalizeCurrent(current), normalizeSize(size)), queryWrapper);
        List<NotificationResponse> records = page.getRecords().stream()
                .map(NotificationResponse::from)
                .toList();
        log.debug("My notifications queried, userId={}, current={}, size={}, readStatus={}, total={}",
                currentUser.getId(), page.getCurrent(), page.getSize(), normalizedReadStatus, page.getTotal());
        return new PageResponse<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getPages(), records);
    }

    /**
     * 查询当前用户未读通知数。
     */
    public long countMyUnread(AuthUser currentUser) {
        long unreadCount = notificationMapper.selectCount(Wrappers.<NotificationMessage>lambdaQuery()
                .eq(NotificationMessage::getReceiverUserId, currentUser.getId())
                .eq(NotificationMessage::getReadStatus, UNREAD));
        log.debug("Unread notifications counted, userId={}, unreadCount={}", currentUser.getId(), unreadCount);
        return unreadCount;
    }

    /**
     * 标记单条通知为已读。
     *
     * <p>只允许通知接收人操作自己的通知；如果 ID 不存在或不属于当前用户，统一返回 404，避免泄露其他用户
     * 是否存在某条通知。</p>
     */
    @Transactional
    public NotificationResponse markRead(Long id, AuthUser currentUser) {
        NotificationMessage message = getMyNotification(id, currentUser);
        if (message.getReadStatus() != READ) {
            message.setReadStatus(READ);
            message.setReadAt(LocalDateTime.now());
            notificationMapper.updateById(message);
            // 单条已读是用户可见状态变化，记录通知 ID 和接收人即可，不记录通知正文。
            log.info("Notification marked read, notificationId={}, receiverUserId={}", message.getId(), currentUser.getId());
        } else {
            log.debug("Notification already read, notificationId={}, receiverUserId={}", message.getId(), currentUser.getId());
        }
        return NotificationResponse.from(message);
    }

    /**
     * 将当前用户全部未读通知标记为已读。
     */
    @Transactional
    public long markAllRead(AuthUser currentUser) {
        List<NotificationMessage> unreadMessages = notificationMapper.selectList(Wrappers.<NotificationMessage>lambdaQuery()
                .eq(NotificationMessage::getReceiverUserId, currentUser.getId())
                .eq(NotificationMessage::getReadStatus, UNREAD));
        LocalDateTime now = LocalDateTime.now();
        for (NotificationMessage message : unreadMessages) {
            // 批量已读逐条更新，当前数据量较小；后续如需优化可改为批量 SQL。
            message.setReadStatus(READ);
            message.setReadAt(now);
            notificationMapper.updateById(message);
        }
        log.info("All notifications marked read, receiverUserId={}, count={}", currentUser.getId(), unreadMessages.size());
        return unreadMessages.size();
    }

    private NotificationMessage getMyNotification(Long id, AuthUser currentUser) {
        if (id == null || id <= 0) {
            throw BusinessException.badRequest("Notification id must be positive");
        }
        NotificationMessage message = notificationMapper.selectOne(Wrappers.<NotificationMessage>lambdaQuery()
                .eq(NotificationMessage::getId, id)
                .eq(NotificationMessage::getReceiverUserId, currentUser.getId())
                .last("LIMIT 1"));
        if (message == null) {
            log.warn("Notification lookup failed, notificationId={}, operatorUserId={}", id, currentUser.getId());
            throw BusinessException.notFound("Notification does not exist");
        }
        return message;
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

    private Integer normalizeReadStatus(Integer readStatus) {
        if (readStatus != UNREAD && readStatus != READ) {
            throw BusinessException.badRequest("Read status must be 0 or 1");
        }
        return readStatus;
    }

    private String resolveType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return "SYSTEM";
        }
        String normalized = type.trim().toUpperCase();
        if (!List.of("SYSTEM", "TASK", "USER").contains(normalized)) {
            throw BusinessException.badRequest("Notification type must be SYSTEM, TASK or USER");
        }
        return normalized;
    }

    private String resolveBizType(String bizType) {
        if (bizType == null || bizType.trim().isEmpty()) {
            return "GENERAL";
        }
        return bizType.trim().toUpperCase();
    }
}
