package com.example.javademo.app.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 插件配置。
 *
 * <p>v0.2 开始需要用户分页查询，MyBatis Plus 的 Page 对象只有配合分页拦截器，
 * 才会把查询转换成带 LIMIT/OFFSET 的 SQL，并额外执行总数统计。这里指定 MySQL 方言，
 * 测试环境 H2 运行在 MySQL 兼容模式下，也可以复用该配置。</p>
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 注册 MyBatis Plus 总拦截器。
     *
     * @return 包含分页能力的拦截器实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
