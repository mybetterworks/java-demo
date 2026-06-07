package com.example.javademo.app.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.javademo.app.cache.UserCacheService;
import com.example.javademo.app.common.BusinessException;
import com.example.javademo.app.dto.CaptchaTrackPoint;
import com.example.javademo.app.dto.LoginRequest;
import com.example.javademo.app.dto.LoginResponse;
import com.example.javademo.app.dto.RegisterRequest;
import com.example.javademo.app.dto.SliderCaptchaChallengeResponse;
import com.example.javademo.app.dto.SliderCaptchaVerifyResponse;
import com.example.javademo.app.dto.UserProfileResponse;
import com.example.javademo.app.entity.User;
import com.example.javademo.app.mapper.UserMapper;
import com.example.javademo.app.security.AuthUser;
import com.example.javademo.app.security.JwtService;
import com.example.javademo.app.security.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户账号业务服务。
 *
 * <p>v0.1 的所有账号核心逻辑都集中在这里：注册、登录、查询当前用户。
 * Controller 只负责 HTTP 适配，Mapper 只负责数据库访问，安全相关能力交给 PasswordService 和 JwtService，
 * 这样职责边界更清楚，也便于后续拆分 auth-service 和 user-service。</p>
 */
@Service
public class UserAccountService {

    /** 账号业务日志只记录用户 ID、用户名和结果摘要，不打印密码或密码哈希。 */
    private static final Logger log = LoggerFactory.getLogger(UserAccountService.class);

    /** 当前版本约定 1 表示账号启用，后续可以扩展禁用、锁定、待激活等状态。 */
    private static final int STATUS_ENABLED = 1;

    /** v0.2 默认角色，当前只做字段存储，不做权限判断。 */
    private static final String DEFAULT_ROLE = "USER";

    /** 逻辑删除默认值，0 表示未删除。 */
    private static final int NOT_DELETED = 0;

    private final UserMapper userMapper;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final LoginRiskService loginRiskService;
    private final UserCacheService userCacheService;

    public UserAccountService(UserMapper userMapper, PasswordService passwordService, JwtService jwtService, LoginRiskService loginRiskService, UserCacheService userCacheService) {
        this.userMapper = userMapper;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
        this.loginRiskService = loginRiskService;
        this.userCacheService = userCacheService;
    }

    /**
     * 注册新用户。
     *
     * <p>注册时会先规范化用户名，再检查是否重复。密码不会明文入库，而是交给 PasswordService
     * 生成 BCrypt 哈希。方法加 @Transactional 是为了保证用户写入过程具备事务边界，
     * 后续如果扩展注册送角色、写审计日志等数据库操作，也能保持一致提交或回滚。</p>
     *
     * @param request 注册请求
     * @return 注册成功后的用户基础信息
     */
    @Transactional
    public UserProfileResponse register(RegisterRequest request) {
        String username = normalizeUsername(request.getUsername());
        if (findByUsername(username) != null) {
            // 重复注册属于可预期业务失败，日志只记录规范化后的用户名。
            log.warn("User registration rejected, reason=username_exists, username={}", username);
            throw BusinessException.conflict("Username already exists");
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordService.hash(request.getPassword()));
        user.setNickname(resolveNickname(request.getNickname(), username));
        user.setStatus(STATUS_ENABLED);
        user.setRole(DEFAULT_ROLE);
        user.setDeleted(NOT_DELETED);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
        // 注册成功后把用户基础资料写入 Redis 缓存，后续 /api/users/me 或 Dubbo 用户校验可以直接复用。
        userCacheService.putUser(UserProfileResponse.from(user));
        log.info("User registered, userId={}, username={}", user.getId(), user.getUsername());
        return UserProfileResponse.from(user);
    }

    /**
     * 用户登录并签发 JWT。
     *
     * <p>为了避免泄露账号是否存在，用户名不存在、账号不可用、密码错误都统一返回
     * Invalid username or password。v0.5.4 在这里新增登录风险判断：同一 username + clientIp
     * 在 5 分钟内失败达到 3 次后，后续登录必须先完成滑块验证码。</p>
     *
     * @param request 登录请求
     * @param clientIp 客户端 IP 或网关转发 IP，用于和 username 一起构成登录风险维度
     * @return 登录响应，包含 token 类型、JWT、过期时间和用户基础信息
     */
    public LoginResponse login(LoginRequest request, String clientIp) {
        String username = normalizeUsername(request.getUsername());

        // 判断当前登录请求是否需要验证码，返回的 riskSnapshot 里包含当前失败次数和阈值等信息，供后续日志记录和异常响应使用。
        LoginRiskService.LoginRiskSnapshot riskSnapshot = loginRiskService.snapshot(username, clientIp);

        // 需要验证码
        if (riskSnapshot.captchaRequired()) {
            // 进入风险状态后必须先消费一次性 captchaToken。这里故意先校验验证码，再校验密码，避免攻击者在高风险状态下继续无限尝试密码。token 不会被写入日志。
            boolean captchaVerified = loginRiskService.consumeVerifiedToken(username, clientIp, request.getCaptchaToken());
            // 若验证码校验失败，则拒绝登录，并返回验证码上下文。
            if (!captchaVerified) {
                log.warn("User login rejected, username={}, reason=captcha_required", username);
                // 返回登录失败摘要和验证码上下文，包括失败次数、验证阈值等。
                throw BusinessException.captchaRequired(
                        "登录失败次数过多，请先完成滑块验证",
                        loginRiskService.captchaRequiredResponse(riskSnapshot)
                );
            }
        }

        User user = findByUsername(username);
        if (user == null || user.getStatus() == null || user.getStatus() != STATUS_ENABLED || !passwordService.matches(request.getPassword(), user.getPasswordHash())) {
            // 登录失败时统一输出摘要，不区分用户不存在、账号禁用还是密码错误，避免接口和日志侧信道放大账号枚举风险。
            LoginRiskService.LoginRiskSnapshot updatedSnapshot = loginRiskService.recordFailure(username, clientIp);
            log.warn("User login rejected, username={}, reason=invalid_credentials_or_disabled", username);
            if (updatedSnapshot.captchaRequired()) {
                // 第 3 次失败后立即告诉前端进入验证码流程；后续即使密码正确，也必须带验证码 token。
                throw BusinessException.captchaRequired(
                        "登录失败次数过多，请完成滑块验证后再登录",
                        loginRiskService.captchaRequiredResponse(updatedSnapshot)
                );
            }
            throw BusinessException.unauthorized("Invalid username or password");
        }

        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginAt(now);
        user.setUpdatedAt(now);
        userMapper.updateById(user);
        // 登录会更新 lastLoginAt，必须刷新用户缓存，避免当前用户接口展示旧的最近登录时间。
        userCacheService.putUser(UserProfileResponse.from(user));

        String token = jwtService.createToken(user.getId(), user.getUsername());
        loginRiskService.clearLoginState(username, clientIp);
        log.info("User login succeeded, userId={}, username={}", user.getId(), user.getUsername());
        return new LoginResponse("Bearer", token, jwtService.getExpirationSeconds(), UserProfileResponse.from(user));
    }

    /**
     * 创建滑块验证码 challenge。
     *
     * <p>Controller 只负责 HTTP 入参，用户名规范化和风险 key 绑定仍放在账号服务里，
     * 这样登录和验证码两个入口使用完全一致的 username 规则。</p>
     *
     * @param username 用户输入的用户名
     * @param clientIp 客户端 IP 或网关转发 IP
     * @return 前端渲染滑块所需的 challenge 信息
     */
    public SliderCaptchaChallengeResponse createSliderCaptcha(String username, String clientIp) {
        String normalizedUsername = normalizeUsername(username);
        return loginRiskService.createChallenge(normalizedUsername, clientIp);
    }

    /**
     * 校验滑块验证码并返回一次性 captchaToken。
     *
     * <p>这里不接收用户名，因为 challenge 创建时已经绑定了 username + clientIp；
     * 登录时再消费 token 并校验同一风险 key，保证 token 不能跨账号或跨客户端复用。</p>
     *
     * @param challengeId challenge 接口返回的 ID
     * @param sliderX 用户最终拖动到的横坐标
     * @param durationMs 拖动耗时，单位毫秒
     * @param tracks 基础拖动轨迹点
     * @return 一次性验证码 token
     */
    public SliderCaptchaVerifyResponse verifySliderCaptcha(String challengeId, int sliderX, long durationMs, List<CaptchaTrackPoint> tracks) {
        return loginRiskService.verifyChallenge(challengeId, sliderX, durationMs, tracks);
    }

    /**
     * 查询当前登录用户资料。
     *
     * <p>即使 token 能解析成功，也仍然要回表查询用户状态，避免已禁用或已删除用户继续使用旧 token。</p>
     *
     * @param currentUser AuthInterceptor 从 JWT 中解析出的当前用户信息
     * @return 当前用户基础资料
     */
    public UserProfileResponse getProfile(AuthUser currentUser) {
        // 当前用户资料属于高频接口，先读缓存；缓存未命中再回表，并在成功后写入缓存。
        UserProfileResponse cachedUser = userCacheService.getUser(currentUser.getId()).orElse(null);
        if (cachedUser != null) {
            if (cachedUser.getStatus() == null || cachedUser.getStatus() != STATUS_ENABLED) {
                log.warn("Current user profile rejected from cache, userId={}, reason=missing_or_disabled", currentUser.getId());
                throw BusinessException.notFound("Current user does not exist");
            }
            log.debug("Current user profile loaded from cache, userId={}, username={}", cachedUser.getId(), cachedUser.getUsername());
            return cachedUser;
        }

        User user = userMapper.selectById(currentUser.getId());
        if (user == null || user.getStatus() == null || user.getStatus() != STATUS_ENABLED) {
            log.warn("Current user profile rejected, userId={}, reason=missing_or_disabled", currentUser.getId());
            throw BusinessException.notFound("Current user does not exist");
        }
        userCacheService.putUser(UserProfileResponse.from(user));
        log.debug("Current user profile loaded, userId={}, username={}", user.getId(), user.getUsername());
        return UserProfileResponse.from(user);
    }

    /**
     * 按用户名查询用户。
     *
     * <p>username 有唯一索引，理论上最多一条记录；这里追加 LIMIT 1 是为了让 SQL 语义更明确。</p>
     */
    private User findByUsername(String username) {
        return userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username).last("LIMIT 1"));
    }

    /**
     * 规范化用户名。
     *
     * <p>当前规则是去除首尾空格并转小写，保证 Alice、alice、 ALICE  都视为同一个登录名。</p>
     */
    private String normalizeUsername(String username) {
        if (username == null) {
            throw BusinessException.badRequest("Username is required");
        }
        return username.trim().toLowerCase();
    }

    /**
     * 解析昵称。
     *
     * <p>昵称为空时默认使用用户名，保证接口响应里总能看到一个可展示的名称。</p>
     */
    private String resolveNickname(String nickname, String username) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return username;
        }
        return nickname.trim();
    }
}
