package com.example.javademo.app.service;

import com.example.javademo.app.cache.UserCacheService;
import com.example.javademo.app.common.BusinessException;
import com.example.javademo.app.dto.UserProfileResponse;
import com.example.javademo.app.entity.User;
import com.example.javademo.app.mapper.UserMapper;
import com.example.javademo.app.security.AuthUser;
import com.example.javademo.app.storage.MinioProperties;
import com.example.javademo.app.storage.ObjectStorageService;
import com.example.javademo.app.storage.StoredObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 用户头像业务服务。
 *
 * <p>v0.9 只把文件内容放到 MinIO，用户表只保存 avatarUrl 和 avatarObjectKey。
 * 这样既避免数据库保存 BLOB，又能让前端继续通过用户资料接口拿到可展示头像地址。</p>
 */
@Service
public class UserAvatarService {

    private static final Logger log = LoggerFactory.getLogger(UserAvatarService.class);

    /** 头像只允许常见 Web 图片格式，避免把任意脚本或压缩包伪装成头像上传。 */
    private static final Map<String, String> ALLOWED_AVATAR_TYPES = Map.of(
            "image/png", "png",
            "image/jpeg", "jpg",
            "image/webp", "webp"
    );

    private final UserMapper userMapper;
    private final UserCacheService userCacheService;
    private final ObjectStorageService objectStorageService;
    private final MinioProperties minioProperties;

    public UserAvatarService(UserMapper userMapper, UserCacheService userCacheService, ObjectStorageService objectStorageService, MinioProperties minioProperties) {
        this.userMapper = userMapper;
        this.userCacheService = userCacheService;
        this.objectStorageService = objectStorageService;
        this.minioProperties = minioProperties;
    }

    /**
     * 上传当前登录用户头像。
     *
     * <p>上传流程分三步：先做文件大小和类型校验，再把文件流写入 MinIO，最后更新用户表头像字段。
     * 数据库只保存 objectKey 和后端代理 URL，不保存原始文件名，避免用户构造特殊文件名影响对象路径。</p>
     */
    @Transactional
    public UserProfileResponse uploadMyAvatar(AuthUser currentUser, MultipartFile file) {
        User user = getExistingUser(currentUser.getId());
        String contentType = normalizeAvatarContentType(file);
        String extension = ALLOWED_AVATAR_TYPES.get(contentType);
        String objectKey = "avatars/user-" + user.getId() + "/" + UUID.randomUUID() + "." + extension;

        try {
            // MinIO SDK 直接消费 MultipartFile 的输入流，避免先落盘到本地临时目录。
            objectStorageService.putObject(
                    minioProperties.getAvatarBucket(),
                    objectKey,
                    file.getInputStream(),
                    file.getSize(),
                    contentType
            );
        } catch (IOException exception) {
            log.warn("Avatar upload rejected, userId={}, reason=input_stream_failed", user.getId());
            throw BusinessException.badRequest("Avatar file cannot be read");
        }

        LocalDateTime now = LocalDateTime.now();
        user.setAvatarObjectKey(objectKey);
        user.setAvatarUrl(buildAvatarUrl(user.getId()));
        user.setUpdatedAt(now);
        userMapper.updateById(user);

        // 头像属于用户资料字段，上传成功后必须刷新缓存，否则 /api/users/me 可能继续返回旧头像。
        UserProfileResponse response = UserProfileResponse.from(user);
        userCacheService.putUser(response);
        log.info("User avatar uploaded, userId={}, bucket={}, objectKey={}, sizeBytes={}, contentType={}",
                user.getId(), minioProperties.getAvatarBucket(), objectKey, file.getSize(), contentType);
        return response;
    }

    /**
     * 读取用户头像对象。
     *
     * <p>该方法用于公开头像代理接口。只根据用户当前 avatarObjectKey 读取对象，不允许前端直接传入 objectKey，
     * 避免通过路径参数枚举 MinIO 内部对象路径。</p>
     */
    public StoredObject getAvatarObject(Long userId) {
        User user = getExistingUser(userId);
        if (user.getAvatarObjectKey() == null || user.getAvatarObjectKey().isBlank()) {
            throw BusinessException.notFound("User avatar does not exist");
        }
        return objectStorageService.getObject(minioProperties.getAvatarBucket(), user.getAvatarObjectKey());
    }

    private User getExistingUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw BusinessException.badRequest("User id must be positive");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("User does not exist");
        }
        return user;
    }

    /**
     * 校验并规范化头像 Content-Type。
     *
     * <p>Content-Type 来自浏览器上传元数据，不能作为绝对安全依据；当前学习版本先做白名单校验，
     * 后续如果进入安全加固阶段，可以再加入魔数检测、图片解码重写或病毒扫描。</p>
     */
    private String normalizeAvatarContentType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("Avatar file must not be empty");
        }
        if (file.getSize() > minioProperties.getMaxAvatarSizeBytes()) {
            throw BusinessException.badRequest("Avatar file is too large");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().trim().toLowerCase();
        if (!ALLOWED_AVATAR_TYPES.containsKey(contentType)) {
            throw BusinessException.badRequest("Avatar file type must be PNG, JPEG or WebP");
        }
        return contentType;
    }

    private String buildAvatarUrl(Long userId) {
        String prefix = minioProperties.getAvatarPublicUrlPrefix();
        String normalizedPrefix = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
        return normalizedPrefix + "/" + userId + "?v=" + System.currentTimeMillis();
    }
}
