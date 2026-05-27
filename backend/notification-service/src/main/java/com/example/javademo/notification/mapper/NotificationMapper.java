package com.example.javademo.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.javademo.notification.entity.NotificationMessage;

/**
 * 通知表 Mapper。
 *
 * <p>当前全部使用 MyBatis Plus BaseMapper 提供的通用 CRUD，后续如果通知检索条件变复杂，
 * 再补充 XML 或自定义 SQL。</p>
 */
public interface NotificationMapper extends BaseMapper<NotificationMessage> {
}
