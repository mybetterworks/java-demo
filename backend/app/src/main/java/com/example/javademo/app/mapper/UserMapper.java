package com.example.javademo.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.javademo.app.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 MyBatis Plus Mapper。
 *
 * <p>继承 BaseMapper 后即可获得常用 CRUD 方法，例如 insert、selectById、selectOne、selectPage。
 * v0.2 的分页查询仍然优先使用 MyBatis Plus 条件构造器，暂不写 XML SQL，
 * 让学习重点放在 CRUD 能力和分页插件上。</p>
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
