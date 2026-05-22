package com.example.javademo.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.javademo.app.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 MyBatis Plus Mapper。
 *
 * <p>继承 BaseMapper 后即可获得常用 CRUD 方法，例如 insert、selectById、selectOne。
 * v0.1 业务查询较少，暂时不需要自定义 SQL；后续用户管理分页、角色查询等能力可以从这里扩展。</p>
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
