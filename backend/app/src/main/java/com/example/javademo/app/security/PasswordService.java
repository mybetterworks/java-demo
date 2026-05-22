package com.example.javademo.app.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 密码哈希服务。
 *
 * <p>该类集中封装密码加密和校验逻辑。当前使用 BCrypt，它会自动加盐并带有计算成本，
 * 比直接哈希更适合保存用户密码。业务层永远不应该直接保存或比较明文密码。</p>
 */
@Service
public class PasswordService {

    /** Spring Security 提供的 BCrypt 实现，默认强度足够支撑本地学习和早期 MVP。 */
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 对明文密码进行 BCrypt 哈希。
     *
     * @param rawPassword 用户提交的明文密码
     * @return 可安全入库的密码哈希
     */
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 校验明文密码是否匹配数据库中的 BCrypt 哈希。
     *
     * @param rawPassword 用户登录时提交的明文密码
     * @param passwordHash 数据库中保存的密码哈希
     * @return true 表示密码匹配
     */
    public boolean matches(String rawPassword, String passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
