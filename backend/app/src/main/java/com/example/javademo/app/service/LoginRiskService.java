package com.example.javademo.app.service;

import com.example.javademo.app.common.BusinessException;
import com.example.javademo.app.dto.CaptchaRequiredResponse;
import com.example.javademo.app.dto.CaptchaTrackPoint;
import com.example.javademo.app.dto.SliderCaptchaChallengeResponse;
import com.example.javademo.app.dto.SliderCaptchaVerifyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录风险与拼图验证码状态服务。
 *
 * <p>本服务承接两类学习目标：第一，保留 v0.5.4 已完成的“登录失败计数 -> 触发验证码 ->
 * 验证码通过后继续登录”闭环；第二，在 v0.5.5 把验证码从“滑到最右侧”的学习型滑块升级为
 * “固定背景图 + 随机缺口 + 服务端保存答案”的图片拼图验证码。</p>
 *
 * <p>当前版本仍然使用单机内存保存失败计数、challenge 和一次性 token，这是为了避免在进入
 * v0.7 Redis 之前过早引入额外中间件。多实例部署时这些状态不会共享，后续会迁移到 Redis TTL key。</p>
 */
@Service
public class LoginRiskService {

    /** 登录风险日志只记录 username、IP 摘要、challengeId 和非敏感原因，禁止记录答案、图片全文或 token。 */
    private static final Logger log = LoggerFactory.getLogger(LoginRiskService.class);

    /** 登录失败统计窗口：5 分钟。 */
    private static final Duration FAILURE_WINDOW = Duration.ofMinutes(5);

    /** 失败阈值：窗口内达到 3 次后，后续登录必须先完成验证码。 */
    private static final int FAILURE_THRESHOLD = 3;

    /** challenge 和一次性验证码 token 都使用 2 分钟短 TTL，降低被复用的风险。 */
    private static final Duration CAPTCHA_TTL = Duration.ofMinutes(2);

    /** 固定背景图资源路径，随应用 jar 一起打包。 */
    private static final String BACKGROUND_RESOURCE = "captcha/backgrounds/login-bg-01.png";

    /** v0.5.5 统一把验证码图片渲染为 320x160，便于 React/Vue 两端保持同样交互尺寸。 */
    private static final int IMAGE_WIDTH = 320;

    /** v0.5.5 验证码图片高度。 */
    private static final int IMAGE_HEIGHT = 160;

    /** 前端轨道宽度与图片宽度一致，sliderX 表示拼图块左上角横坐标。 */
    private static final int TRACK_WIDTH = IMAGE_WIDTH;

    /** 拼图块宽度。 */
    private static final int PUZZLE_WIDTH = 48;

    /** 拼图块高度。 */
    private static final int PUZZLE_HEIGHT = 48;

    /** 随机缺口最小横坐标，避免缺口贴近起点导致过于容易通过。 */
    private static final int MIN_TARGET_X = 64;

    /** 随机缺口右侧保留距离，避免缺口等于 trackWidth - puzzleWidth 这种可由固定公式推导的位置。 */
    private static final int RIGHT_PADDING = 24;

    /** 缺口纵向最小边距，保证拼图块完整显示在背景图中。 */
    private static final int VERTICAL_PADDING = 28;

    /** 允许少量像素误差，避免浏览器缩放、手指拖动和取整造成体验问题。 */
    private static final int TOLERANCE = 5;

    /** 过短的拖动耗时更像脚本直接提交，先用轻量规则拦截。 */
    private static final long MIN_DURATION_MS = 300;

    /** 拖动耗时不能超过 challenge TTL，否则应重新获取 challenge。 */
    private static final long MAX_DURATION_MS = CAPTCHA_TTL.toMillis();

    /** 轨迹至少需要起点、中间点和终点，避免瞬间跳到答案。 */
    private static final int MIN_TRACK_POINTS = 3;

    /** 限制轨迹点数量，避免恶意提交超大 JSON 请求体。 */
    private static final int MAX_TRACK_POINTS = 120;

    /** 单个 challenge 最多允许两次失败，超过后要求前端重新获取，降低暴力试探空间。 */
    private static final int MAX_CHALLENGE_FAILURES = 2;

    /** Java 标准安全随机数，用于生成 challengeId、token 和随机缺口位置。 */
    private final SecureRandom secureRandom = new SecureRandom();

    /** key 为 username + clientIp，值为 5 分钟窗口内的失败时间队列。 */
    private final Map<String, FailureState> failureStates = new ConcurrentHashMap<>();

    /** key 为 challengeId，值为待校验的拼图 challenge 状态。 */
    private final Map<String, SliderChallenge> challenges = new ConcurrentHashMap<>();

    /** key 为 captchaToken，值为校验通过后的短 TTL 登录前置凭证。 */
    private final Map<String, VerifiedCaptchaToken> verifiedTokens = new ConcurrentHashMap<>();

    private final StringRedisTemplate redisTemplate;
    private final Environment environment;

    public LoginRiskService(StringRedisTemplate redisTemplate, Environment environment) {
        this.redisTemplate = redisTemplate;
        this.environment = environment;
    }

    /**
     * 判断当前登录主体是否已经进入验证码风险状态。
     *
     * @param username 规范化后的用户名
     * @param clientIp 客户端 IP 或网关转发 IP
     * @return 登录风险快照
     */
    public LoginRiskSnapshot snapshot(String username, String clientIp) {
        LoginRiskSnapshot redisSnapshot = snapshotFromRedis(username, clientIp);
        if (redisSnapshot != null) {
            return redisSnapshot;
        }

        Instant now = Instant.now();
        String riskKey = buildRiskKey(username, clientIp);
        FailureState state = failureStates.get(riskKey);
        int failureCount = state == null ? 0 : state.countWithinWindow(now);
        if (state != null && failureCount == 0) {
            // 懒清理空窗口，避免长期运行后保留大量已经过期的 username + IP 组合。
            failureStates.remove(riskKey, state);
        }
        return new LoginRiskSnapshot(failureCount, failureCount >= FAILURE_THRESHOLD, FAILURE_THRESHOLD, FAILURE_WINDOW.toSeconds());
    }

    /**
     * 记录一次登录失败，并返回更新后的风险快照。
     *
     * <p>用户名不存在、账号禁用和密码错误都会走到这里；对外仍返回统一错误，
     * 避免通过接口或日志区分账号是否存在。</p>
     */
    public LoginRiskSnapshot recordFailure(String username, String clientIp) {
        LoginRiskSnapshot redisSnapshot = recordFailureInRedis(username, clientIp);
        if (redisSnapshot != null) {
            return redisSnapshot;
        }

        Instant now = Instant.now();
        String riskKey = buildRiskKey(username, clientIp);
        // ConcurrentHashMap，用于存储登录失败状态。
        // 使用computeIfAbsent方法为riskKey创建一个新的FailureState对象。
        // ignored -> new FailureState()是一个Lambda表达式，表示当键不存在时，创建并返回一个新的FailureState实例。
        // 如果riskKey存在，返回对应的FailureState对象。如果riskKey不存在，创建新的FailureState对象，存入failureStates，并返回该对象。
        FailureState state = failureStates.computeIfAbsent(riskKey, ignored -> new FailureState());
        int failureCount = state.addFailureAndCount(now);
        String ipHash = hashClientIp(clientIp);

        if (failureCount >= FAILURE_THRESHOLD) {
            log.warn("Login captcha required, username={}, clientIpHash={}, failureCount={}, windowSeconds={}",
                    username, ipHash, failureCount, FAILURE_WINDOW.toSeconds());
        } else {
            log.warn("Login failure counted, username={}, clientIpHash={}, failureCount={}, threshold={}",
                    username, ipHash, failureCount, FAILURE_THRESHOLD);
        }
        return new LoginRiskSnapshot(failureCount, failureCount >= FAILURE_THRESHOLD, FAILURE_THRESHOLD, FAILURE_WINDOW.toSeconds());
    }

    /**
     * 为当前登录主体创建一次拼图验证码 challenge。
     *
     * <p>后端会随机生成真实缺口横坐标 targetX，并只保存在内存状态中；响应只返回带缺口背景图、
     * 拼图块图、垂直位置和尺寸参数。这样前端无法再通过 trackWidth - puzzleWidth 之类固定公式得到答案。</p>
     */
    public SliderCaptchaChallengeResponse createChallenge(String username, String clientIp) {
        cleanupExpiredCaptchaState();

        String riskKey = buildRiskKey(username, clientIp);
        String challengeId = randomUrlSafeToken(18);
        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plus(CAPTCHA_TTL);

        // 随机缺口刻意避开最左侧、最右侧和固定终点，让答案只能由服务端状态确认。
        int targetX = randomBetween(MIN_TARGET_X, IMAGE_WIDTH - PUZZLE_WIDTH - RIGHT_PADDING); // 计算原理为：图片宽度（IMAGE_WIDTH）- 拼图块宽度（PUZZLE_WIDTH）- 右侧保留距离（RIGHT_PADDING）
        int targetY = randomBetween(VERTICAL_PADDING, IMAGE_HEIGHT - PUZZLE_HEIGHT - VERTICAL_PADDING);
        PuzzleImages images = generatePuzzleImages(targetX, targetY);

        SliderChallenge challenge = new SliderChallenge(riskKey, targetX, targetY, createdAt, expiresAt);
        if (!storeChallengeInRedis(challengeId, challenge)) {
            challenges.put(challengeId, challenge);
        }
        log.info("Puzzle captcha challenge created, challengeId={}, username={}, clientIpHash={}, imageSize={}x{}, puzzleSize={}x{}, expiresInSeconds={}",
                challengeId, username, hashClientIp(clientIp), IMAGE_WIDTH, IMAGE_HEIGHT, PUZZLE_WIDTH, PUZZLE_HEIGHT, CAPTCHA_TTL.toSeconds());

        return new SliderCaptchaChallengeResponse(
                challengeId,
                images.backgroundImage(),
                images.puzzleImage(),
                TRACK_WIDTH,
                IMAGE_WIDTH,
                IMAGE_HEIGHT,
                PUZZLE_WIDTH,
                PUZZLE_HEIGHT,
                targetY,
                CAPTCHA_TTL.toSeconds(),
                "请拖动拼图块到背景图缺口位置，完成安全验证"
        );
    }

    /**
     * 校验拼图验证码并生成一次性 captchaToken。
     *
     * <p>校验维度包括最终横坐标、拖动耗时、轨迹点数量、轨迹时间顺序和基本移动方向。
     * 任一维度失败都会返回 4602，不会签发 token，也不会在日志中记录真实答案。</p>
     */
    public SliderCaptchaVerifyResponse verifyChallenge(String challengeId, int sliderX, long durationMs, List<CaptchaTrackPoint> tracks) {
        // 每次校验前都清理一次过期状态，保证内存里不会长期保留过期的 challenge 和 token。
        cleanupExpiredCaptchaState();

        // 根据 challengeId 获取 challenge 状态，校验是否存在且未过期。过期或不存在都返回 4602，并在日志中记录非敏感原因。
        SliderChallenge challenge = challenges.get(challengeId);
        boolean redisChallenge = false;
        LoadedChallenge loadedChallenge = loadChallengeFromRedis(challengeId);
        if (loadedChallenge != null) {
            challenge = loadedChallenge.challenge();
            redisChallenge = true;
        }
        if (challenge == null || challenge.isExpired(Instant.now())) {
            challenges.remove(challengeId);
            deleteChallengeFromRedis(challengeId);
            log.warn("Puzzle captcha verification failed, challengeId={}, reason=missing_or_expired", challengeId);
            throw BusinessException.captchaInvalid("拼图验证码已过期，请重新获取");
        }

        // 校验拖动结果，任何维度不满足都返回 4602，并在日志中记录非敏感原因。challengeId 和非敏感原因可以帮助排查攻击模式，但禁止记录 sliderX、targetX、token 等敏感信息。
        String invalidReason = validateDragResult(challenge, sliderX, durationMs, tracks);
        if (invalidReason != null) {
            int failureCount = redisChallenge
                    ? addRedisChallengeFailure(challengeId, challenge)
                    : challenge.addFailureAndCount();
            boolean invalidated = failureCount >= MAX_CHALLENGE_FAILURES;
            if (invalidated) {
                challenges.remove(challengeId, challenge);
                deleteChallengeFromRedis(challengeId);
            }
            log.warn("Puzzle captcha verification failed, challengeId={}, reason={}, failureCount={}, invalidated={}",
                    challengeId, invalidReason, failureCount, invalidated);
            throw BusinessException.captchaInvalid("拼图验证失败，请重新拖动");
        }

        // 成功后立即删除 challenge，避免同一个图片挑战被重复换取多个 token。
        challenges.remove(challengeId, challenge);
        deleteChallengeFromRedis(challengeId);
        String captchaToken = randomUrlSafeToken(32);
        if (!storeVerifiedTokenInRedis(captchaToken, challenge.riskKey())) {
            verifiedTokens.put(captchaToken, new VerifiedCaptchaToken(challenge.riskKey(), Instant.now().plus(CAPTCHA_TTL)));
        }
        log.info("Puzzle captcha verification succeeded, challengeId={}, durationMs={}, trackPoints={}, tokenExpiresInSeconds={}",
                challengeId, durationMs, tracks == null ? 0 : tracks.size(), CAPTCHA_TTL.toSeconds());
        return new SliderCaptchaVerifyResponse(captchaToken, CAPTCHA_TTL.toSeconds());
    }

    /**
     * 消费一次性验证码 token。
     *
     * <p>登录接口在风险状态下调用该方法。只有 token 存在、未过期且绑定同一个 username + clientIp 时才返回 true。
     * 校验成功或失败都会先删除 token，确保同一个验证码结果不能被重复使用。</p>
     */
    public boolean consumeVerifiedToken(String username, String clientIp, String captchaToken) {
        if (captchaToken == null || captchaToken.trim().isEmpty()) {
            return false;
        }

        Boolean redisConsumed = consumeVerifiedTokenFromRedis(username, clientIp, captchaToken);
        if (redisConsumed != null) {
            return redisConsumed;
        }

        VerifiedCaptchaToken token = verifiedTokens.remove(captchaToken.trim());
        Instant now = Instant.now();
        String expectedRiskKey = buildRiskKey(username, clientIp);

        if (token == null || token.isExpired(now)) {
            log.warn("Login captcha token rejected, username={}, clientIpHash={}, reason=missing_or_expired",
                    username, hashClientIp(clientIp));
            return false;
        }

        if (!expectedRiskKey.equals(token.riskKey())) {
            log.warn("Login captcha token rejected, username={}, clientIpHash={}, reason=risk_key_mismatch",
                    username, hashClientIp(clientIp));
            return false;
        }

        log.info("Login captcha token consumed, username={}, clientIpHash={}", username, hashClientIp(clientIp));
        return true;
    }

    /**
     * 登录成功后清理失败计数、challenge 和 token。
     *
     * <p>成功登录代表用户已经完成账号密码校验，并且在高风险状态下完成了二次验证。
     * 清理状态可以让后续正常登录恢复为低风险流程。</p>
     */
    public void clearLoginState(String username, String clientIp) {
        String riskKey = buildRiskKey(username, clientIp);
        failureStates.remove(riskKey);
        challenges.entrySet().removeIf(entry -> riskKey.equals(entry.getValue().riskKey()));
        verifiedTokens.entrySet().removeIf(entry -> riskKey.equals(entry.getValue().riskKey()));
        clearRedisLoginState(riskKey);
        log.info("Login risk state cleared, username={}, clientIpHash={}", username, hashClientIp(clientIp));
    }

    /** 构造给前端使用的“验证码必需”失败上下文。 */
    public CaptchaRequiredResponse captchaRequiredResponse(LoginRiskSnapshot snapshot) {
        return new CaptchaRequiredResponse(
                true,
                snapshot.failureCount(),
                snapshot.failureThreshold(),
                snapshot.windowSeconds()
        );
    }

    /**
     * 从 Redis 读取登录失败计数快照。
     *
     * <p>失败计数 key 使用 5 分钟 TTL。这里如果 Redis 不可用，会返回 null 让调用方继续走内存降级路径，
     * 同时日志明确标记 fallback，便于真实联调时观察 Redis 是否参与了登录风控。</p>
     */
    private LoginRiskSnapshot snapshotFromRedis(String username, String clientIp) {
        if (!redisEnabled()) {
            return null;
        }
        String key = failureKey(buildRiskKey(username, clientIp));
        try {
            String value = redisTemplate.opsForValue().get(key);
            int failureCount = value == null || value.isBlank() ? 0 : Integer.parseInt(value);
            log.debug("Login failure snapshot loaded, username={}, clientIpHash={}, failureCount={}, cache=redis",
                    username, hashClientIp(clientIp), failureCount);
            return new LoginRiskSnapshot(failureCount, failureCount >= FAILURE_THRESHOLD, FAILURE_THRESHOLD, FAILURE_WINDOW.toSeconds());
        } catch (Exception exception) {
            log.warn("Redis login failure snapshot failed, username={}, clientIpHash={}, reason={}, fallback=memory",
                    username, hashClientIp(clientIp), exception.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * 在 Redis 中记录一次登录失败。
     *
     * <p>使用 INCR 保证多实例下同一 username + clientIp 的失败次数原子递增；
     * 第一次写入时设置 TTL，窗口过期后 Redis 自动删除计数。</p>
     */
    private LoginRiskSnapshot recordFailureInRedis(String username, String clientIp) {
        if (!redisEnabled()) {
            return null;
        }
        String key = failureKey(buildRiskKey(username, clientIp));
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, FAILURE_WINDOW);
            }
            int failureCount = count == null ? 1 : count.intValue();
            if (failureCount >= FAILURE_THRESHOLD) {
                log.warn("Login captcha required, username={}, clientIpHash={}, failureCount={}, windowSeconds={}, cache=redis",
                        username, hashClientIp(clientIp), failureCount, FAILURE_WINDOW.toSeconds());
            } else {
                log.warn("Login failure counted, username={}, clientIpHash={}, failureCount={}, threshold={}, cache=redis",
                        username, hashClientIp(clientIp), failureCount, FAILURE_THRESHOLD);
            }
            return new LoginRiskSnapshot(failureCount, failureCount >= FAILURE_THRESHOLD, FAILURE_THRESHOLD, FAILURE_WINDOW.toSeconds());
        } catch (Exception exception) {
            log.warn("Redis login failure count failed, username={}, clientIpHash={}, reason={}, fallback=memory",
                    username, hashClientIp(clientIp), exception.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * 把拼图 challenge 写入 Redis hash。
     *
     * <p>真实答案 targetX 仍然只保存在服务端状态里，不进入响应或日志。Redis hash 只设置短 TTL，
     * 避免 challenge 长期残留。</p>
     */
    private boolean storeChallengeInRedis(String challengeId, SliderChallenge challenge) {
        if (!redisEnabled()) {
            return false;
        }
        try {
            String key = challengeKey(challengeId);
            redisTemplate.opsForHash().put(key, "riskKey", challenge.riskKey());
            redisTemplate.opsForHash().put(key, "targetX", Integer.toString(challenge.targetX()));
            redisTemplate.opsForHash().put(key, "targetY", Integer.toString(challenge.targetY()));
            redisTemplate.opsForHash().put(key, "createdAt", Long.toString(challenge.createdAt().toEpochMilli()));
            redisTemplate.opsForHash().put(key, "expiresAt", Long.toString(challenge.expiresAt().toEpochMilli()));
            redisTemplate.opsForHash().put(key, "failureCount", "0");
            redisTemplate.expire(key, CAPTCHA_TTL);
            log.debug("Puzzle captcha challenge stored, challengeId={}, ttlSeconds={}, cache=redis",
                    challengeId, CAPTCHA_TTL.toSeconds());
            return true;
        } catch (Exception exception) {
            log.warn("Redis captcha challenge write failed, challengeId={}, reason={}, fallback=memory",
                    challengeId, exception.getClass().getSimpleName());
            return false;
        }
    }

    /**
     * 从 Redis 读取 challenge。
     */
    private LoadedChallenge loadChallengeFromRedis(String challengeId) {
        if (!redisEnabled()) {
            return null;
        }
        try {
            String key = challengeKey(challengeId);
            Map<Object, Object> values = redisTemplate.opsForHash().entries(key);
            if (values.isEmpty()) {
                return null;
            }
            SliderChallenge challenge = new SliderChallenge(
                    stringValue(values, "riskKey"),
                    integerValue(values, "targetX"),
                    integerValue(values, "targetY"),
                    Instant.ofEpochMilli(longValue(values, "createdAt")),
                    Instant.ofEpochMilli(longValue(values, "expiresAt")),
                    integerValue(values, "failureCount")
            );
            log.debug("Puzzle captcha challenge loaded, challengeId={}, cache=redis", challengeId);
            return new LoadedChallenge(challenge);
        } catch (Exception exception) {
            log.warn("Redis captcha challenge read failed, challengeId={}, reason={}, fallback=memory",
                    challengeId, exception.getClass().getSimpleName());
            return null;
        }
    }

    private int addRedisChallengeFailure(String challengeId, SliderChallenge challenge) {
        if (!redisEnabled()) {
            return challenge.addFailureAndCount();
        }
        try {
            Long count = redisTemplate.opsForHash().increment(challengeKey(challengeId), "failureCount", 1);
            return count == null ? challenge.addFailureAndCount() : count.intValue();
        } catch (Exception exception) {
            log.warn("Redis captcha challenge failure update failed, challengeId={}, reason={}, fallback=memory",
                    challengeId, exception.getClass().getSimpleName());
            return challenge.addFailureAndCount();
        }
    }

    private void deleteChallengeFromRedis(String challengeId) {
        if (!redisEnabled()) {
            return;
        }
        try {
            redisTemplate.delete(challengeKey(challengeId));
        } catch (Exception exception) {
            log.warn("Redis captcha challenge delete failed, challengeId={}, reason={}",
                    challengeId, exception.getClass().getSimpleName());
        }
    }

    /**
     * 把验证码通过后的短 TTL token 写入 Redis。
     */
    private boolean storeVerifiedTokenInRedis(String captchaToken, String riskKey) {
        if (!redisEnabled()) {
            return false;
        }
        try {
            redisTemplate.opsForValue().set(verifiedTokenKey(captchaToken), riskKey, CAPTCHA_TTL);
            log.debug("Login captcha token stored, tokenHash={}, ttlSeconds={}, cache=redis",
                    hashToken(captchaToken), CAPTCHA_TTL.toSeconds());
            return true;
        } catch (Exception exception) {
            log.warn("Redis captcha token write failed, tokenHash={}, reason={}, fallback=memory",
                    hashToken(captchaToken), exception.getClass().getSimpleName());
            return false;
        }
    }

    /**
     * 从 Redis 原子消费一次性 captchaToken。
     */
    private Boolean consumeVerifiedTokenFromRedis(String username, String clientIp, String captchaToken) {
        if (!redisEnabled()) {
            return null;
        }
        String key = verifiedTokenKey(captchaToken.trim());
        String expectedRiskKey = buildRiskKey(username, clientIp);
        try {
            String riskKey = redisTemplate.opsForValue().get(key);
            redisTemplate.delete(key);
            if (riskKey == null || riskKey.isBlank()) {
                log.warn("Login captcha token rejected, username={}, clientIpHash={}, reason=missing_or_expired, cache=redis",
                        username, hashClientIp(clientIp));
                return false;
            }
            if (!expectedRiskKey.equals(riskKey)) {
                log.warn("Login captcha token rejected, username={}, clientIpHash={}, reason=risk_key_mismatch, cache=redis",
                        username, hashClientIp(clientIp));
                return false;
            }
            log.info("Login captcha token consumed, username={}, clientIpHash={}, cache=redis", username, hashClientIp(clientIp));
            return true;
        } catch (Exception exception) {
            log.warn("Redis captcha token consume failed, username={}, clientIpHash={}, reason={}, fallback=memory",
                    username, hashClientIp(clientIp), exception.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * 清理 Redis 中当前风险主体的失败计数。
     *
     * <p>challenge 和 token 的 key 不按 riskKey 建索引，成功登录后依赖短 TTL 自动过期；
     * 这样可以避免为了反向索引维护更多 Redis 集合。失败计数必须立即删除，保证登录成功后恢复低风险状态。</p>
     */
    private void clearRedisLoginState(String riskKey) {
        if (!redisEnabled()) {
            return;
        }
        try {
            redisTemplate.delete(failureKey(riskKey));
            log.debug("Login failure state cleared, riskKeyHash={}, cache=redis", hashToken(riskKey));
        } catch (Exception exception) {
            log.warn("Redis login failure clear failed, riskKeyHash={}, reason={}",
                    hashToken(riskKey), exception.getClass().getSimpleName());
        }
    }

    /**
     * 校验拼图拖动结果。
     * <p>该方法只返回非敏感失败原因，日志层不会拿到 targetX。坐标错误也只记录大致类别，
     * 避免通过“提交 0 后读取 distance 日志”反推出答案。</p>
     * <p>该方法对用户的拖动行为进行多维度校验，包括位置、时间、轨迹点数量、轨迹点顺序等。
     * 如果任何一项校验失败，返回对应的失败原因；如果所有校验通过，返回 null。</p>
     *
     * @param challenge 当前拼图验证码的挑战状态，包含目标位置等信息
     * @param sliderX 用户拖动滑块的最终横坐标
     * @param durationMs 用户拖动滑块所花费的时间（毫秒）
     * @param tracks 用户拖动滑块的轨迹点列表，每个点包含 x、y 坐标和时间戳
     * @return 校验失败原因的字符串；如果校验通过，返回 null
     */
    private String validateDragResult(SliderChallenge challenge, int sliderX, long durationMs, List<CaptchaTrackPoint> tracks) {
        // 计算用户滑块最终位置与目标位置的距离
        int distance = Math.abs(sliderX - challenge.targetX());
        if (distance > TOLERANCE) {
            // 如果距离超过容差范围，判断偏差程度
            return distance > 30 ? "位置偏差过大" : "位置偏差较近";
        }

        // 校验拖动时间是否过短
        if (durationMs < MIN_DURATION_MS) {
            return "拖动时间过短";
        }

        // 校验拖动时间是否过长
        if (durationMs > MAX_DURATION_MS) {
            return "拖动时间过长";
        }

        // 校验轨迹点数量是否过少
        if (tracks == null || tracks.size() < MIN_TRACK_POINTS) {
            return "轨迹点过少";
        }

        // 校验轨迹点数量是否过多
        if (tracks.size() > MAX_TRACK_POINTS) {
            return "轨迹点过多";
        }

        // 获取轨迹的起点和终点
        CaptchaTrackPoint first = tracks.get(0);
        CaptchaTrackPoint last = tracks.get(tracks.size() - 1);

        // 校验轨迹是否从最左侧开始
        if (first.getX() > TOLERANCE) {
            return "轨迹未从最左侧开始";
        }

        // 校验轨迹的结束位置是否与滑块最终位置匹配
        if (Math.abs(last.getX() - sliderX) > TOLERANCE) {
            return "轨迹结束位置不匹配";
        }

        // 校验轨迹是否有明显进展
        if (last.getX() <= first.getX() + 20) {
            return "轨迹无明显进展";
        }

        // 遍历轨迹点，校验轨迹的详细规则
        long previousTime = -1; // 上一个轨迹点的时间戳
        int previousX = -1; // 上一个轨迹点的横坐标
        int maxX = 0; // 轨迹中的最大横坐标
        int backwardJumps = 0; // 记录轨迹中的后退次数
        for (CaptchaTrackPoint point : tracks) {
            // 校验轨迹点的横坐标是否超出范围
            if (point.getX() < 0 || point.getX() > TRACK_WIDTH - PUZZLE_WIDTH + TOLERANCE) {
                return "轨迹横坐标超出范围";
            }

            // 校验轨迹点的纵坐标是否超出范围
            if (point.getY() < 0 || point.getY() > IMAGE_HEIGHT) {
                return "轨迹纵坐标超出范围";
            }

            // 校验轨迹点的时间戳是否倒退
            if (previousTime >= 0 && point.getT() < previousTime) {
                return "轨迹时间倒退";
            }

            // 校验轨迹是否存在明显的后退行为
            if (previousX >= 0 && point.getX() + 12 < previousX) {
                backwardJumps++;
            }

            // 更新最大横坐标
            maxX = Math.max(maxX, point.getX());

            // 更新上一个轨迹点的时间戳和横坐标
            previousTime = point.getT();
            previousX = point.getX();
        }

        // 校验轨迹的时间是否超出拖动时长
        if (last.getT() > durationMs + 250) {
            return "轨迹时间超出拖动时长";
        }

        // 校验轨迹的最大横坐标是否到达目标位置
        if (maxX < sliderX - TOLERANCE) {
            return "轨迹未到达目标位置";
        }

        // 校验轨迹中的后退次数是否异常
        if (backwardJumps > Math.max(1, tracks.size() / 3)) {
            return "轨迹方向异常";
        }

        // 所有校验通过，返回 null
        return null;
    }

    /**
     * 生成带缺口背景图和拼图块图。
     *
     * <p>背景图使用项目内置 PNG，后端只在 challenge 响应中下发已经画好缺口的版本；
     * 拼图块从原图对应位置裁切。真实 targetX 不会进入响应或日志。</p>
     */
    private PuzzleImages generatePuzzleImages(int targetX, int targetY) {
        // 加载验证码背景图片
        BufferedImage source = loadCaptchaBackground();

        // 创建拼图缺口的形状，位置由 targetX 和 targetY 决定
        Shape backgroundShape = createPuzzleShape(targetX, targetY);

        // 创建拼图块的形状，默认从 (0, 0) 开始
        Shape localShape = createPuzzleShape(0, 0);

        // 复制背景图片为 RGB 格式，便于后续绘制缺口
        BufferedImage backgroundWithHole = copyAsRgb(source);
        Graphics2D backgroundGraphics = backgroundWithHole.createGraphics();
        try {
            // 开启高质量渲染，减少锯齿
            enableQualityRendering(backgroundGraphics);

            // 设置缺口的填充颜色（半透明黑色），并填充缺口形状
            backgroundGraphics.setColor(new Color(0, 0, 0, 92));
            backgroundGraphics.fill(backgroundShape);

            // 设置缺口边框的颜色和粗细，并绘制边框
            backgroundGraphics.setStroke(new BasicStroke(2.0f));
            backgroundGraphics.setColor(new Color(255, 255, 255, 190));
            backgroundGraphics.draw(backgroundShape);

            // 绘制缺口的阴影效果，稍微偏移位置
            backgroundGraphics.setStroke(new BasicStroke(1.0f));
            backgroundGraphics.setColor(new Color(15, 23, 42, 120));
            backgroundGraphics.draw(createPuzzleShape(targetX + 2, targetY + 2));
        } finally {
            // 释放背景图的绘图资源
            backgroundGraphics.dispose();
        }

        // 创建拼图块图片，大小为拼图块的宽度和高度
        BufferedImage puzzlePiece = new BufferedImage(PUZZLE_WIDTH, PUZZLE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pieceGraphics = puzzlePiece.createGraphics();
        try {
            // 开启高质量渲染，减少锯齿
            enableQualityRendering(pieceGraphics);

            // 设置拼图块的裁剪区域为 localShape
            pieceGraphics.setClip(localShape);

            // 将背景图片的对应部分绘制到拼图块上，位置为 (-targetX, -targetY)
            pieceGraphics.drawImage(source, -targetX, -targetY, null);

            // 清除裁剪区域
            pieceGraphics.setClip(null);

            // 设置拼图块边框的颜色和粗细，并绘制边框
            pieceGraphics.setStroke(new BasicStroke(2.0f));
            pieceGraphics.setColor(new Color(255, 255, 255, 220));
            pieceGraphics.draw(localShape);

            // 绘制拼图块的阴影效果，稍微偏移位置
            pieceGraphics.setStroke(new BasicStroke(1.0f));
            pieceGraphics.setColor(new Color(15, 23, 42, 120));
            pieceGraphics.draw(createPuzzleShape(1, 1));
        } finally {
            // 释放拼图块的绘图资源
            pieceGraphics.dispose();
        }

        // 将背景图和拼图块图像编码为 PNG 格式的 Data URL，并返回
        return new PuzzleImages(toPngDataUrl(backgroundWithHole), toPngDataUrl(puzzlePiece));
    }

    /** 从 classpath 加载固定背景图，并在尺寸不一致时缩放到统一尺寸。 */
    private BufferedImage loadCaptchaBackground() {
        try (InputStream inputStream = new ClassPathResource(BACKGROUND_RESOURCE).getInputStream()) {
            BufferedImage source = ImageIO.read(inputStream);
            if (source == null) {
                throw new IllegalStateException("验证码背景图不是合法 PNG: " + BACKGROUND_RESOURCE);
            }
            if (source.getWidth() == IMAGE_WIDTH && source.getHeight() == IMAGE_HEIGHT) {
                return copyAsRgb(source);
            }
            BufferedImage resized = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = resized.createGraphics();
            try {
                enableQualityRendering(graphics);
                graphics.drawImage(source, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null);
            } finally {
                graphics.dispose();
            }
            return resized;
        } catch (IOException exception) {
            throw new IllegalStateException("读取验证码背景图失败: " + BACKGROUND_RESOURCE, exception);
        }
    }

    /** 创建拼图块形状。MVP 使用圆角矩形，优先保证可视化清晰和裁切稳定。 */
    private Shape createPuzzleShape(int x, int y) {
        return new RoundRectangle2D.Double(x, y, PUZZLE_WIDTH, PUZZLE_HEIGHT, 12, 12);
    }

    /** 把图片复制成 RGB，便于后续叠加缺口阴影。 */
    private BufferedImage copyAsRgb(BufferedImage source) {
        BufferedImage copy = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = copy.createGraphics();
        try {
            enableQualityRendering(graphics);
            graphics.drawImage(source, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null);
        } finally {
            graphics.dispose();
        }
        return copy;
    }

    /** 把 PNG 图片编码成 data URL，前端可以直接渲染。 */
    private String toPngDataUrl(BufferedImage image) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("编码验证码图片失败", exception);
        }
    }

    /** 统一开启较高质量渲染，减少验证码边缘锯齿。 */
    private void enableQualityRendering(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setComposite(AlphaComposite.SrcOver);
    }

    /**
     * 构建风险统计 key。
     *
     * <p>MVP 决策使用 username + clientIp。username 已由 UserAccountService 规范化，
     * clientIp 由 HTTP 入口解析。后续 Redis 迁移时该 key 可以直接演进为 Redis key 的业务部分。</p>
     */
    private String buildRiskKey(String username, String clientIp) {
        return username + "|" + (clientIp == null || clientIp.isBlank() ? "unknown" : clientIp.trim());
    }

    private boolean redisEnabled() {
        return environment.getProperty("java-demo.redis.enabled", Boolean.class, true);
    }

    private String redisKeyPrefix() {
        return environment.getProperty("java-demo.redis.key-prefix", "java-demo:v0_7");
    }

    private String failureKey(String riskKey) {
        return redisKeyPrefix() + ":login:fail:" + hashToken(riskKey);
    }

    private String challengeKey(String challengeId) {
        return redisKeyPrefix() + ":captcha:challenge:" + challengeId;
    }

    private String verifiedTokenKey(String captchaToken) {
        return redisKeyPrefix() + ":captcha:token:" + hashToken(captchaToken);
    }

    private String stringValue(Map<Object, Object> values, String field) {
        Object value = values.get(field);
        if (value == null) {
            throw new IllegalStateException("Redis captcha challenge missing field: " + field);
        }
        return value.toString();
    }

    private int integerValue(Map<Object, Object> values, String field) {
        return Integer.parseInt(stringValue(values, field));
    }

    private long longValue(Map<Object, Object> values, String field) {
        return Long.parseLong(stringValue(values, field));
    }

    /** 懒清理过期 challenge 和 token，避免为 MVP 引入后台定时任务。 */
    private void cleanupExpiredCaptchaState() {
        Instant now = Instant.now();
        challenges.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
        verifiedTokens.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    /** 生成 URL 安全随机字符串，避免前端 JSON 或表单传递时出现需要转义的字符。 */
    private String randomUrlSafeToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** 生成闭区间随机整数。 */
    private int randomBetween(int minInclusive, int maxInclusive) {
        return minInclusive + secureRandom.nextInt(maxInclusive - minInclusive + 1);
    }

    /**
     * 对 IP 做短哈希摘要。
     *
     * <p>日志需要帮助判断是否同一客户端反复失败，但不需要保存完整 IP。
     * 这里使用 SHA-256 前 8 位十六进制作为学习环境的脱敏摘要。</p>
     */
    private String hashClientIp(String clientIp) {
        String value = clientIp == null || clientIp.isBlank() ? "unknown" : clientIp.trim();
        return hashToken(value).substring(0, 8);
    }

    /**
     * 对 Redis key 的敏感片段做 SHA-256 摘要。
     *
     * <p>captchaToken 和 riskKey 都不应该原样出现在日志或 Redis key 后缀里，因此统一使用摘要。</p>
     */
    private String hashToken(String value) {
        String normalized = value == null || value.isBlank() ? "blank" : value.trim();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte current : hashed) {
                builder.append(String.format("%02x", current));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            return Integer.toHexString(normalized.hashCode());
        }
    }

    /**
     * 单个登录主体在 5 分钟窗口内的失败时间队列。
     *
     * <p>Deque 不是线程安全集合，因此对修改和计数方法加 synchronized。当前项目为学习环境，
     * 这种粒度足够清晰；后续 Redis 版本会由 Redis 原子操作替代。</p>
     */
    private static class FailureState {

        private final Deque<Instant> failures = new ArrayDeque<>();

        /** 添加一次失败记录并返回当前窗口内失败次数。 */
        synchronized int addFailureAndCount(Instant now) {
            prune(now);
            failures.addLast(now);
            return failures.size();
        }

        /** 返回当前窗口内失败次数。 */
        synchronized int countWithinWindow(Instant now) {
            prune(now);
            return failures.size();
        }

        /** 清理窗口外的失败记录，保持队列只包含当前统计窗口内的数据。 */
        private void prune(Instant now) {
            Instant cutoff = now.minus(FAILURE_WINDOW);
            Iterator<Instant> iterator = failures.iterator();
            while (iterator.hasNext()) {
                Instant failureAt = iterator.next();
                if (failureAt.isBefore(cutoff)) {
                    iterator.remove();
                } else {
                    break;
                }
            }
        }
    }

    /**
     * 登录风险快照，避免调用方直接感知内部 map 和计数实现。
     *
     * @param failureCount 当前窗口内的失败次数
     * @param captchaRequired 是否需要验证码
     * @param failureThreshold 失败阈值，达到该次数后需要验证码
     * @param windowSeconds 失败时间窗口秒数
     */
    public record LoginRiskSnapshot(int failureCount, boolean captchaRequired, int failureThreshold, long windowSeconds) {
    }

    /** 内存中的拼图 challenge 状态，真实 targetX 只保存在这里。 */
    private static class SliderChallenge {

        private final String riskKey;
        private final int targetX;
        private final int targetY;
        private final Instant createdAt;
        private final Instant expiresAt;
        private int failureCount;

        SliderChallenge(String riskKey, int targetX, int targetY, Instant createdAt, Instant expiresAt) {
            this(riskKey, targetX, targetY, createdAt, expiresAt, 0);
        }

        SliderChallenge(String riskKey, int targetX, int targetY, Instant createdAt, Instant expiresAt, int failureCount) {
            this.riskKey = riskKey;
            this.targetX = targetX;
            this.targetY = targetY;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.failureCount = failureCount;
        }

        String riskKey() {
            return riskKey;
        }

        int targetX() {
            return targetX;
        }

        int targetY() {
            return targetY;
        }

        Instant createdAt() {
            return createdAt;
        }

        Instant expiresAt() {
            return expiresAt;
        }

        boolean isExpired(Instant now) {
            return !expiresAt.isAfter(now);
        }

        synchronized int addFailureAndCount() {
            failureCount++;
            return failureCount;
        }
    }

    /** 校验通过后生成的一次性验证码 token 状态。 */
    private record VerifiedCaptchaToken(String riskKey, Instant expiresAt) {
        boolean isExpired(Instant now) {
            return !expiresAt.isAfter(now);
        }
    }

    /** Redis 中读取出的 challenge 包装对象，后续可扩展来源标记或调试字段。 */
    private record LoadedChallenge(SliderChallenge challenge) {
    }

    /** 后端生成的两张验证码图片 data URL。 */
    private record PuzzleImages(String backgroundImage, String puzzleImage) {
    }
}
