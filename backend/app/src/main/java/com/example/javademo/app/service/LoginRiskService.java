package com.example.javademo.app.service;

import com.example.javademo.app.common.BusinessException;
import com.example.javademo.app.dto.CaptchaRequiredResponse;
import com.example.javademo.app.dto.SliderCaptchaChallengeResponse;
import com.example.javademo.app.dto.SliderCaptchaVerifyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录风险与滑块验证码状态服务。
 *
 * <p>v0.5.4 的目标是先跑通“失败计数 -> 触发验证码 -> 验证通过后登录”的学习闭环，
 * 因此本类使用单机内存保存失败计数、验证码 challenge 和一次性验证码 token。
 * 这能保持当前版本不额外引入 Redis，但也意味着多实例部署时状态不共享；后续 v0.7 Redis 会把这些
 * map 迁移为带 TTL 的 Redis key。</p>
 */
@Service
public class LoginRiskService {

    /** 登录风险日志只记录用户名、IP 摘要、次数和 challengeId，禁止记录验证码答案或 token。 */
    private static final Logger log = LoggerFactory.getLogger(LoginRiskService.class);

    /** 失败计数时间窗口：5 分钟。 */
    private static final Duration FAILURE_WINDOW = Duration.ofMinutes(5);

    /** 失败阈值：窗口内达到 3 次后，后续登录必须先完成滑块验证码。 */
    private static final int FAILURE_THRESHOLD = 3;

    /** 验证码 challenge 和校验通过 token 都采用 2 分钟短 TTL，降低被复用的风险。 */
    private static final Duration CAPTCHA_TTL = Duration.ofMinutes(2);

    /** 当前学习型滑块轨道宽度。 */
    private static final int TRACK_WIDTH = 320;

    /** 当前学习型滑块宽度。 */
    private static final int PUZZLE_WIDTH = 48;

    /** 允许少量像素误差，避免浏览器 slider 组件、缩放和取整造成体验问题。 */
    private static final int TOLERANCE = 5;

    /** Java 标准安全随机数，用于生成 challengeId 和一次性验证码 token。 */
    private final SecureRandom secureRandom = new SecureRandom();

    /** key 为 username + clientIp 的稳定组合，值为 5 分钟窗口内的失败时间列表。 */
    private final Map<String, FailureState> failureStates = new ConcurrentHashMap<>();

    /** key 为 challengeId，值为待验证的滑块 challenge。 */
    private final Map<String, SliderChallenge> challenges = new ConcurrentHashMap<>();

    /** key 为 captchaToken，值为校验通过后的短 TTL 登录前置凭证。 */
    private final Map<String, VerifiedCaptchaToken> verifiedTokens = new ConcurrentHashMap<>();

    /**
     * 判断当前登录主体是否已经进入验证码风险状态。
     *
     * @param username 规范化后的用户名
     * @param clientIp 客户端 IP 或网关转发 IP
     * @return 风险状态快照
     */
    public LoginRiskSnapshot snapshot(String username, String clientIp) {
        Instant now = Instant.now();
        String riskKey = buildRiskKey(username, clientIp);
        FailureState state = failureStates.get(riskKey);
        int failureCount = state == null ? 0 : state.countWithinWindow(now);
        if (state != null && failureCount == 0) {
            // 懒清理空窗口，避免长期运行后保留大量已经过期的用户名 + IP 组合。
            failureStates.remove(riskKey, state);
        }
        return new LoginRiskSnapshot(failureCount, failureCount >= FAILURE_THRESHOLD, FAILURE_THRESHOLD, FAILURE_WINDOW.toSeconds());
    }

    /**
     * 记录一次登录失败，并返回更新后的风险快照。
     *
     * <p>用户名不存在、账号禁用和密码错误都会走到这里，但前端仍只看到统一错误。
     * 日志用于学习和排查，不做账号存在性区分，避免把账号枚举风险从接口转移到日志侧。</p>
     */
    public LoginRiskSnapshot recordFailure(String username, String clientIp) {
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
     * 为当前登录主体创建滑块验证码 challenge。
     *
     * <p>当前 MVP 使用“滑动到终点”的轻量滑块。后端仍保存 answerOffset，并设置短 TTL；
     * verify 阶段只校验位置，不在任何日志或响应中输出 answerOffset。后续如要升级图片拼图，
     * 可以保留 challengeId/token 流程，只替换答案生成和前端展示。</p>
     */
    public SliderCaptchaChallengeResponse createChallenge(String username, String clientIp) {
        cleanupExpiredCaptchaState();

        String riskKey = buildRiskKey(username, clientIp);
        String challengeId = randomUrlSafeToken(18);
        Instant expiresAt = Instant.now().plus(CAPTCHA_TTL);
        int answerOffset = TRACK_WIDTH - PUZZLE_WIDTH;

        challenges.put(challengeId, new SliderChallenge(riskKey, answerOffset, expiresAt));
        log.info("Slider captcha challenge created, challengeId={}, username={}, clientIpHash={}, expiresInSeconds={}",
                challengeId, username, hashClientIp(clientIp), CAPTCHA_TTL.toSeconds());

        return new SliderCaptchaChallengeResponse(
                challengeId,
                TRACK_WIDTH,
                PUZZLE_WIDTH,
                CAPTCHA_TTL.toSeconds(),
                "请将滑块拖动到最右侧完成安全验证"
        );
    }

    /**
     * 校验滑块位置并生成一次性 captchaToken。
     *
     * <p>验证码 challenge 一旦校验成功或失败都会失效：成功时换成短 TTL token，失败时要求前端重新获取
     * challenge。这种“一次性”策略可以减少重复尝试同一个 challenge 的机会。</p>
     */
    public SliderCaptchaVerifyResponse verifyChallenge(String challengeId, int sliderPosition) {
        cleanupExpiredCaptchaState();

        SliderChallenge challenge = challenges.remove(challengeId);
        if (challenge == null || challenge.isExpired(Instant.now())) {
            log.warn("Slider captcha verification failed, challengeId={}, reason=missing_or_expired", challengeId);
            throw BusinessException.captchaInvalid("滑块验证码已过期，请重新获取");
        }

        int distance = Math.abs(sliderPosition - challenge.answerOffset());
        if (distance > TOLERANCE) {
            // 日志只记录偏差大小，不记录真实答案，避免验证码答案进入日志文件。
            log.warn("Slider captcha verification failed, challengeId={}, reason=position_mismatch, distance={}",
                    challengeId, distance);
            throw BusinessException.captchaInvalid("滑块验证失败，请重新拖动");
        }

        String captchaToken = randomUrlSafeToken(32);
        verifiedTokens.put(captchaToken, new VerifiedCaptchaToken(challenge.riskKey(), Instant.now().plus(CAPTCHA_TTL)));
        log.info("Slider captcha verification succeeded, challengeId={}, tokenExpiresInSeconds={}",
                challengeId, CAPTCHA_TTL.toSeconds());
        return new SliderCaptchaVerifyResponse(captchaToken, CAPTCHA_TTL.toSeconds());
    }

    /**
     * 消费一次性验证码 token。
     *
     * <p>登录接口在风险状态下调用该方法。只有 token 存在、未过期且绑定同一 username + clientIp 时才返回 true。
     * 校验成功后立即删除 token，避免同一个验证码结果被多次登录复用。</p>
     */
    public boolean consumeVerifiedToken(String username, String clientIp, String captchaToken) {
        if (captchaToken == null || captchaToken.trim().isEmpty()) {
            return false;
        }

        // 立即删除 token，确保无论校验成功与否都不能重复使用同一个 token。remove方法返回被删除的值，如果返回 null 说明 token 不存在或已经被消费过了。
        VerifiedCaptchaToken token = verifiedTokens.remove(captchaToken.trim());
        Instant now = Instant.now();
        String expectedRiskKey = buildRiskKey(username, clientIp);

        // 校验 token 是否存在且未过期，以及 token 绑定的风险 key 是否与当前登录请求的风险 key 匹配。
        if (token == null || token.isExpired(now)) {
            log.warn("Login captcha token rejected, username={}, clientIpHash={}, reason=missing_or_expired",
                    username, hashClientIp(clientIp));
            return false;
        }

        // 校验 token 绑定的风险 key 与当前登录请求的风险 key 是否匹配，确保 token 不能跨账号或跨客户端复用。
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
     * <p>成功登录代表用户已经完成账号密码校验，并且在风险状态下已经完成二次验证。
     * 清理状态可以让后续正常登录恢复低风险流程。</p>
     */
    public void clearLoginState(String username, String clientIp) {
        String riskKey = buildRiskKey(username, clientIp);
        failureStates.remove(riskKey);
        challenges.entrySet().removeIf(entry -> riskKey.equals(entry.getValue().riskKey()));
        verifiedTokens.entrySet().removeIf(entry -> riskKey.equals(entry.getValue().riskKey()));
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
     * 构建风险统计 key。
     *
     * <p>MVP 决策使用 username + clientIp。username 已由 UserAccountService 规范化，
     * clientIp 取 HTTP 入口解析结果。后续 Redis 迁移时该 key 可以直接演进为 Redis key 的业务部分。</p>
     */
    private String buildRiskKey(String username, String clientIp) {
        return username + "|" + (clientIp == null || clientIp.isBlank() ? "unknown" : clientIp.trim());
    }

    /** 清理过期 challenge 和 token，采用懒清理避免为 MVP 引入后台定时任务。 */
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

    /**
     * 对 IP 做短哈希摘要。
     *
     * <p>日志要能帮助判断是否同一客户端反复失败，但不需要保存完整 IP。
     * 这里使用 SHA-256 前 8 位十六进制作为学习环境的脱敏摘要。</p>
     */
    private String hashClientIp(String clientIp) {
        String value = clientIp == null || clientIp.isBlank() ? "unknown" : clientIp.trim();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                builder.append(String.format("%02x", hashed[i]));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            return "sha256-unavailable";
        }
    }

    /**
     * 单个登录主体在 5 分钟窗口内的失败时间列表。
     *
     * <p>Deque 不是线程安全集合，因此对修改和计数方法加 synchronized。
     * 当前项目为学习环境，这种粒度足够清晰；后续 Redis 版本会由 Redis 原子操作替代。</p>
     */
    private static class FailureState {

        // Deque是一个双端队列。Instant是时间点的类。
        private final Deque<Instant> failures = new ArrayDeque<>();

        // addFailureAndCount方法用于添加一次失败记录并返回当前窗口内的失败次数。它首先调用prune方法清理过期的失败记录，然后将当前时间点添加到队列末尾，并返回队列的大小作为当前窗口内的失败次数。
        synchronized int addFailureAndCount(Instant now) {
            prune(now);
            failures.addLast(now);
            return failures.size();
        }

        // countWithinWindow方法用于获取当前窗口内的失败次数。它首先调用prune方法清理过期的失败记录，然后返回当前队列中的失败记录数量。
        synchronized int countWithinWindow(Instant now) {
            prune(now);
            return failures.size();
        }

        // prune方法用于清理过期的失败记录。它计算出截止时间点cutoff，即当前时间点减去失败窗口的持续时间。然后使用迭代器遍历队列中的失败记录，如果发现某个记录的时间点早于截止时间点，就将其从队列中移除；如果遇到一个记录的时间点不早于截止时间点，说明后续的记录也都在窗口内，可以停止遍历。
        private void prune(Instant now) {
            // 清理窗口外的失败时间，保持队列只包含当前窗口内的失败记录。cutoff表示清理的节点时间点，早于这个时间点的记录都应该被移除。minus方法用于计算cutoff时间点，即当前时间点减去失败窗口的持续时间。
            Instant cutoff = now.minus(FAILURE_WINDOW);
            // 获取队列的迭代器
            Iterator<Instant> iterator = failures.iterator();
            // 遍历队列并移除过期记录
            while (iterator.hasNext()) {
                Instant failureAt = iterator.next();
                // 判断当前时间点是否早于截止时间，如果是，则移除该记录；否则，停止遍历，因为队列是按时间顺序排列的。
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
     * @param windowSeconds 失败时间窗口的秒数，用于前端展示剩余时间等用途
     */
    public record LoginRiskSnapshot(int failureCount, boolean captchaRequired, int failureThreshold, long windowSeconds) {
    }

    /** 内存中的滑块 challenge 状态。 */
    private record SliderChallenge(String riskKey, int answerOffset, Instant expiresAt) {
        boolean isExpired(Instant now) {
            return !expiresAt.isAfter(now);
        }
    }

    /** 校验通过后生成的一次性验证码 token 状态。 */
    private record VerifiedCaptchaToken(String riskKey, Instant expiresAt) {
        boolean isExpired(Instant now) {
            return !expiresAt.isAfter(now);
        }
    }
}
