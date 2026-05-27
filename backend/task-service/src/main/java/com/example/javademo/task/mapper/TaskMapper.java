package com.example.javademo.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.javademo.task.entity.TaskItem;

/**
 * 任务表 MyBatis Plus Mapper。
 *
 * <p>当前直接继承 BaseMapper 即可获得常用 CRUD 能力。复杂统计和搜索会留给后续 Redis、
 * Elasticsearch 和 InfluxDB milestone。</p>
 */
public interface TaskMapper extends BaseMapper<TaskItem> {
}
