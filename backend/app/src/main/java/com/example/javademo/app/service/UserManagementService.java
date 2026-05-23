package com.example.javademo.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.javademo.app.common.BusinessException;
import com.example.javademo.app.dto.ChangePasswordRequest;
import com.example.javademo.app.dto.CreateUserRequest;
import com.example.javademo.app.dto.PageResponse;
import com.example.javademo.app.dto.UpdateUserRequest;
import com.example.javademo.app.dto.UserProfileResponse;
import com.example.javademo.app.entity.User;
import com.example.javademo.app.mapper.UserMapper;
import com.example.javademo.app.security.PasswordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户管理业务服务。
 *
 * <p>v0.2 的新增能力全部放在这里，包括分页、详情、创建、更新、逻辑删除和改密。
 * 认证登录仍由 UserAccountService 负责。这样做的好处是：当前仍是单体应用，代码却已经按照
 * “账号认证”和“用户管理”两个业务边界拆开，后续拆分微服务时迁移成本更低。</p>
 */
@Service
public class UserManagementService {

    /** 启用状态，允许登录。 */
    private static final int STATUS_ENABLED = 1;

    /** 禁用状态，不能登录但仍保留用户资料。 */
    private static final int STATUS_DISABLED = 0;

    /** 逻辑删除默认值，0 表示未删除。 */
    private static final int NOT_DELETED = 0;

    /** 默认角色。v0.2 只保存字符串，不做 RBAC 权限判断。 */
    private static final String DEFAULT_ROLE = "USER";

    /** 分页最大 pageSize，避免学习项目里一次查询过多数据。 */
    private static final long MAX_PAGE_SIZE = 100;

    private final UserMapper userMapper;
    private final PasswordService passwordService;

    public UserManagementService(UserMapper userMapper, PasswordService passwordService) {
        this.userMapper = userMapper;
        this.passwordService = passwordService;
    }

    /**
     * 分页查询用户。
     *
     * <p>MyBatis Plus 的逻辑删除插件会自动过滤 deleted = 1 的用户，因此默认列表只展示未删除数据。
     * username 使用模糊查询，status 使用精确查询，满足 v0.2 最小管理筛选需求。</p>
     *
     * @param current  当前页码，从 1 开始
     * @param size     每页条数，会被限制在 1 到 100
     * @param username 可选用户名关键字
     * @param status   可选用户状态
     * @return 分页后的用户基础信息
     */
    public PageResponse<UserProfileResponse> pageUsers(Long current, Long size, String username, Integer status) {
        long safeCurrent = normalizeCurrent(current);
        long safeSize = normalizeSize(size);
        Integer normalizedStatus = status == null ? null : normalizeStatus(status);
        String normalizedUsername = normalizeOptionalUsernameKeyword(username);

        LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery()
                .like(normalizedUsername != null, User::getUsername, normalizedUsername)
                .eq(normalizedStatus != null, User::getStatus, normalizedStatus)
                .orderByDesc(User::getCreatedAt)
                .orderByDesc(User::getId);

        IPage<User> page = userMapper.selectPage(new Page<>(safeCurrent, safeSize), queryWrapper);
        List<UserProfileResponse> records = page.getRecords().stream()
                .map(UserProfileResponse::from)
                .toList();
        return new PageResponse<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getPages(), records);
    }

    /**
     * 根据 ID 查询用户详情。
     *
     * <p>如果用户已被逻辑删除，MyBatis Plus 会让 selectById 返回 null，因此调用方会看到 404。</p>
     */
    public UserProfileResponse getUser(Long id) {
        User user = getExistingUser(id);
        return UserProfileResponse.from(user);
    }

    /**
     * 管理端创建用户。
     *
     * <p>创建逻辑与注册相似，但允许指定状态和角色。密码依旧只保存 BCrypt 哈希，
     * 不因为是管理端接口就放松安全规则。</p>
     */
    @Transactional
    public UserProfileResponse createUser(CreateUserRequest request) {
        String username = normalizeUsername(request.getUsername());
        if (findByUsername(username) != null) {
            throw BusinessException.conflict("Username already exists");
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordService.hash(request.getPassword()));
        user.setNickname(resolveNickname(request.getNickname(), username));
        user.setStatus(request.getStatus() == null ? STATUS_ENABLED : normalizeStatus(request.getStatus()));
        user.setRole(resolveRole(request.getRole()));
        user.setDeleted(NOT_DELETED);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
        return UserProfileResponse.from(user);
    }

    /**
     * 更新用户基础信息。
     *
     * <p>v0.2 不允许在这里修改 username 和 password。用户名涉及唯一索引和登录主体变更；
     * 密码则使用单独的 changePassword 方法，避免敏感字段与普通资料混在一起。</p>
     */
    @Transactional
    public UserProfileResponse updateUser(Long id, UpdateUserRequest request) {
        User user = getExistingUser(id);
        boolean changed = false;

        if (request.getNickname() != null) {
            user.setNickname(resolveRequiredNickname(request.getNickname()));
            changed = true;
        }
        if (request.getStatus() != null) {
            user.setStatus(normalizeStatus(request.getStatus()));
            changed = true;
        }
        if (request.getRole() != null) {
            user.setRole(resolveRequiredRole(request.getRole()));
            changed = true;
        }
        if (!changed) {
            throw BusinessException.badRequest("At least one field must be provided");
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return UserProfileResponse.from(user);
    }

    /**
     * 逻辑删除用户。
     *
     * <p>deleteById 会被 @TableLogic 转换为 UPDATE sys_user SET deleted = 1。
     * 删除后的用户不会出现在默认分页、详情查询和登录查询中。</p>
     */
    @Transactional
    public void deleteUser(Long id) {
        getExistingUser(id);
        userMapper.deleteById(id);
    }

    /**
     * 修改用户密码。
     *
     * <p>改密后旧密码对应的哈希被覆盖，后续登录只能使用新密码。已签发 JWT 暂不立即失效，
     * 这个能力会留到后续 Redis/session 或 token 黑名单阶段处理。</p>
     */
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        User user = getExistingUser(id);
        user.setPasswordHash(passwordService.hash(request.getPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    private User getExistingUser(Long id) {
        if (id == null || id <= 0) {
            throw BusinessException.badRequest("User id must be positive");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw BusinessException.notFound("User does not exist");
        }
        return user;
    }

    private User findByUsername(String username) {
        return userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username).last("LIMIT 1"));
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

    private Integer normalizeStatus(Integer status) {
        if (status != STATUS_ENABLED && status != STATUS_DISABLED) {
            throw BusinessException.badRequest("Status must be 0 or 1");
        }
        return status;
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            throw BusinessException.badRequest("Username is required");
        }
        String normalized = username.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw BusinessException.badRequest("Username is required");
        }
        return normalized;
    }

    private String normalizeOptionalUsernameKeyword(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        return username.trim().toLowerCase();
    }

    private String resolveNickname(String nickname, String username) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return username;
        }
        return nickname.trim();
    }

    private String resolveRequiredNickname(String nickname) {
        if (nickname.trim().isEmpty()) {
            throw BusinessException.badRequest("Nickname must not be blank");
        }
        return nickname.trim();
    }

    private String resolveRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return DEFAULT_ROLE;
        }
        return role.trim().toUpperCase();
    }

    private String resolveRequiredRole(String role) {
        if (role.trim().isEmpty()) {
            throw BusinessException.badRequest("Role must not be blank");
        }
        return role.trim().toUpperCase();
    }
}
