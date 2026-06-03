import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Form, Input, Space, Typography, message } from 'antd';
import { useRef, useState } from 'react';
import type { PointerEvent } from 'react';
import { createSliderCaptchaApi, loginApi, verifySliderCaptchaApi } from '../api/backend';
import { ApiError } from '../api/client';
import type { CaptchaRequiredData, CaptchaTrackPoint, LoginResponse, SliderCaptchaChallengeResponse } from '../types';

interface LoginPageProps {
  onLogin: (response: LoginResponse) => Promise<void>;
}

interface LoginFormValues {
  username: string;
  password: string;
}

const CAPTCHA_REQUIRED_CODE = 4601;

/**
 * 登录页只负责收集账号密码、处理登录风险验证码和触发登录，不直接操作 IndexedDB。
 *
 * <p>v0.5.5 把原来的组件库 Slider 升级为自定义拼图拖拽区域：后端返回带缺口背景图和拼图块图，
 * React 页面只负责展示图片、采集拖动轨迹并提交 sliderX / durationMs / tracks。</p>
 */
export function LoginPage({ onLogin }: LoginPageProps) {
  const [form] = Form.useForm<LoginFormValues>();
  const [messageApi, contextHolder] = message.useMessage();
  const [submitting, setSubmitting] = useState(false);
  const [captchaRequired, setCaptchaRequired] = useState(false);
  const [captchaInfo, setCaptchaInfo] = useState<CaptchaRequiredData | null>(null);
  const [captchaChallenge, setCaptchaChallenge] = useState<SliderCaptchaChallengeResponse | null>(null);
  const [sliderPosition, setSliderPosition] = useState(0);
  const [captchaToken, setCaptchaToken] = useState<string | null>(null);
  const [captchaLoading, setCaptchaLoading] = useState(false);
  const [captchaVerifying, setCaptchaVerifying] = useState(false);
  const [dragging, setDragging] = useState(false);
  const dragStartClientXRef = useRef(0);
  const dragStartSliderXRef = useRef(0);
  const dragStartedAtRef = useRef(0);
  const trackPointsRef = useRef<CaptchaTrackPoint[]>([]);

  async function handleFinish(values: LoginFormValues) {
    if (captchaRequired && !captchaToken) {
      /**
       * 高风险状态下不重复提交账号密码，而是先引导用户完成拼图验证。
       * 这样可以避免在验证码缺失时反复触发后端登录接口。
       */
      await loadCaptchaChallenge(values.username);
      messageApi.warning('请先完成拼图验证，再提交登录');
      return;
    }

    setSubmitting(true);
    try {
      const response = await loginApi({
        username: values.username,
        password: values.password,
        captchaToken: captchaToken ?? undefined
      });
      await onLogin(response);
      resetCaptchaState();
      messageApi.success('登录成功，欢迎回来');
    } catch (error) {
      if (isCaptchaRequiredError(error)) {
        /**
         * 后端返回 code=4601 时说明当前 username + clientIp 已进入风险状态。
         * 前端立即展示拼图并拉取 challenge，让用户不必猜测下一步该做什么。
         */
        setCaptchaRequired(true);
        setCaptchaInfo(readCaptchaInfo(error));
        setCaptchaToken(null);
        await loadCaptchaChallenge(values.username);
        messageApi.warning(error.message);
      } else {
        messageApi.error(error instanceof Error ? error.message : '登录失败，请稍后再试');
      }
    } finally {
      setSubmitting(false);
    }
  }

  async function loadCaptchaChallenge(username: string) {
    if (!username?.trim()) {
      return;
    }

    setCaptchaLoading(true);
    try {
      const challenge = await createSliderCaptchaApi({ username });
      setCaptchaChallenge(challenge);
      resetPuzzleDragState();
      setCaptchaToken(null);
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : '验证码生成失败，请稍后重试');
    } finally {
      setCaptchaLoading(false);
    }
  }

  async function handleVerifyCaptcha() {
    if (!captchaChallenge) {
      const username = form.getFieldValue('username');
      await loadCaptchaChallenge(username);
      return;
    }

    const tracks = ensureFinalTrackPoint();
    if (tracks.length < 3) {
      /**
       * 前端先做一层体验提示；真正的安全判断仍以服务端为准。
       * 这样用户没有拖动时不会直接收到后端 4602，看起来更友好。
       */
      messageApi.warning('请先拖动拼图块到缺口位置');
      return;
    }

    setCaptchaVerifying(true);
    try {
      const response = await verifySliderCaptchaApi({
        challengeId: captchaChallenge.challengeId,
        sliderX: Math.round(sliderPosition),
        durationMs: resolveDragDurationMs(),
        tracks
      });
      setCaptchaToken(response.captchaToken);
      messageApi.success('拼图验证通过，请继续登录');
    } catch (error) {
      setCaptchaToken(null);
      setCaptchaChallenge(null);
      resetPuzzleDragState();
      messageApi.error(error instanceof Error ? error.message : '拼图验证失败，请重新获取验证码');
    } finally {
      setCaptchaVerifying(false);
    }
  }

  function handlePuzzlePointerDown(event: PointerEvent<HTMLImageElement>) {
    if (!captchaChallenge || captchaToken) {
      return;
    }

    /**
     * Pointer Events 同时覆盖鼠标和触摸屏。这里记录拖动起点和初始 sliderX，
     * 后续移动时按水平位移更新拼图块位置，并采集基础轨迹。
     */
    event.preventDefault();
    event.currentTarget.setPointerCapture(event.pointerId);
    dragStartClientXRef.current = event.clientX;
    dragStartSliderXRef.current = sliderPosition;
    dragStartedAtRef.current = performance.now();
    trackPointsRef.current = [{ x: Math.round(sliderPosition), y: captchaChallenge.puzzleY, t: 0 }];
    setDragging(true);
  }

  function handlePuzzlePointerMove(event: PointerEvent<HTMLImageElement>) {
    if (!dragging || !captchaChallenge || captchaToken) {
      return;
    }

    const maxSliderX = captchaChallenge.trackWidth - captchaChallenge.puzzleWidth;
    const nextX = clamp(dragStartSliderXRef.current + event.clientX - dragStartClientXRef.current, 0, maxSliderX);
    setSliderPosition(nextX);
    appendTrackPoint(Math.round(nextX), captchaChallenge.puzzleY, Math.round(performance.now() - dragStartedAtRef.current));
  }

  function handlePuzzlePointerEnd(event: PointerEvent<HTMLImageElement>) {
    if (!dragging || !captchaChallenge) {
      return;
    }

    appendTrackPoint(Math.round(sliderPosition), captchaChallenge.puzzleY, Math.round(performance.now() - dragStartedAtRef.current));
    event.currentTarget.releasePointerCapture(event.pointerId);
    setDragging(false);
  }

  function appendTrackPoint(x: number, y: number, t: number) {
    const tracks = trackPointsRef.current;
    const last = tracks.length > 0 ? tracks[tracks.length - 1] : null;
    if (!last || Math.abs(last.x - x) >= 2 || t - last.t >= 45) {
      tracks.push({ x, y, t });
    }
    if (tracks.length > 120) {
      trackPointsRef.current = tracks.slice(tracks.length - 120);
    }
  }

  function ensureFinalTrackPoint() {
    if (!captchaChallenge) {
      return [];
    }
    appendTrackPoint(Math.round(sliderPosition), captchaChallenge.puzzleY, resolveDragDurationMs());
    return trackPointsRef.current;
  }

  function resolveDragDurationMs() {
    const tracks = trackPointsRef.current;
    const last = tracks.length > 0 ? tracks[tracks.length - 1] : null;
    if (dragStartedAtRef.current <= 0) {
      return last?.t ?? 0;
    }
    return Math.max(last?.t ?? 0, Math.round(performance.now() - dragStartedAtRef.current));
  }

  function handleFormValuesChange(changedValues: Partial<LoginFormValues>) {
    if (changedValues.username !== undefined) {
      /**
       * 验证码 token 与 username + clientIp 绑定。用户名变化后必须丢弃旧 challenge/token，
       * 避免用户误以为旧验证码可以用于新账号。
       */
      resetCaptchaState();
    }
  }

  function resetCaptchaState() {
    setCaptchaRequired(false);
    setCaptchaInfo(null);
    setCaptchaChallenge(null);
    resetPuzzleDragState();
    setCaptchaToken(null);
  }

  function resetPuzzleDragState() {
    setSliderPosition(0);
    setDragging(false);
    dragStartClientXRef.current = 0;
    dragStartSliderXRef.current = 0;
    dragStartedAtRef.current = 0;
    trackPointsRef.current = [];
  }

  function isCaptchaRequiredError(error: unknown): error is ApiError {
    return error instanceof ApiError
      && (error.code === CAPTCHA_REQUIRED_CODE || readCaptchaInfo(error)?.captchaRequired === true);
  }

  function readCaptchaInfo(error: unknown): CaptchaRequiredData | null {
    if (!(error instanceof ApiError) || typeof error.data !== 'object' || error.data === null) {
      return null;
    }
    return error.data as CaptchaRequiredData;
  }

  function clamp(value: number, min: number, max: number) {
    return Math.min(Math.max(value, min), max);
  }

  const captchaReady = !captchaRequired || Boolean(captchaToken);

  return (
    <main className="login-scene">
      {contextHolder}
      <section className="login-hero">
        <div className="hero-kicker">v0.5.5 React Admin</div>
        <Typography.Title level={1}>把微服务学习做成可触摸的系统</Typography.Title>
        <Typography.Paragraph>
          当前版本继续强化登录安全：失败次数过多后，需要先完成后端生成的图片拼图验证，再携带一次性 token 登录。
        </Typography.Paragraph>
      </section>

      <Card className="login-card" variant="borderless">
        <Typography.Title level={3}>登录管理端</Typography.Title>
        <Typography.Paragraph type="secondary">
          使用后端已有用户登录。登录成功后 token 会保存到 IndexedDB，用于刷新页面后恢复会话。
        </Typography.Paragraph>
        <Form
          form={form}
          layout="vertical"
          onFinish={handleFinish}
          onValuesChange={handleFormValuesChange}
          initialValues={{ username: 'alice' }}
        >
          <Form.Item
            label="用户名"
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input prefix={<UserOutlined />} placeholder="例如 alice" autoComplete="username" />
          </Form.Item>
          <Form.Item
            label="密码"
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="请输入密码" autoComplete="current-password" />
          </Form.Item>
          {captchaRequired && (
            <div className="captcha-panel">
              <Alert
                type={captchaToken ? 'success' : 'warning'}
                showIcon
                message={captchaToken ? '拼图验证已通过' : '需要拼图验证'}
                description={
                  captchaInfo
                    ? `当前 5 分钟内失败 ${captchaInfo.failureCount}/${captchaInfo.failureThreshold} 次，请完成验证后继续登录。`
                    : '登录失败次数较多，请完成验证后继续登录。'
                }
              />
              <div className="captcha-track">
                <div className="captcha-track-copy">
                  {captchaChallenge?.instruction ?? '点击获取验证码后，将拼图块拖动到背景缺口位置'}
                </div>
                {captchaChallenge && (
                  <>
                    <div
                      className="captcha-puzzle-stage"
                      style={{ width: captchaChallenge.imageWidth, height: captchaChallenge.imageHeight }}
                    >
                      <img className="captcha-background" src={captchaChallenge.backgroundImage} alt="验证码背景图" draggable={false} />
                      <img
                        className={`captcha-puzzle-piece${dragging ? ' dragging' : ''}`}
                        src={captchaChallenge.puzzleImage}
                        alt="可拖动拼图块"
                        draggable={false}
                        style={{
                          width: captchaChallenge.puzzleWidth,
                          height: captchaChallenge.puzzleHeight,
                          transform: `translate3d(${sliderPosition}px, ${captchaChallenge.puzzleY}px, 0)`
                        }}
                        onPointerDown={handlePuzzlePointerDown}
                        onPointerMove={handlePuzzlePointerMove}
                        onPointerUp={handlePuzzlePointerEnd}
                        onPointerCancel={handlePuzzlePointerEnd}
                      />
                    </div>
                    {/*<div className="captcha-progress">*/}
                    {/*  <span>拖动距离：{Math.round(sliderPosition)}px</span>*/}
                    {/*  <span>有效期：{captchaChallenge.expiresInSeconds}s</span>*/}
                    {/*</div>*/}
                  </>
                )}
              </div>
              <Space className="captcha-actions" wrap>
                <Button loading={captchaLoading} onClick={() => loadCaptchaChallenge(form.getFieldValue('username'))}>
                  {captchaChallenge ? '重新获取验证码' : '获取验证码'}
                </Button>
                <Button
                  type="primary"
                  ghost
                  disabled={!captchaChallenge || Boolean(captchaToken)}
                  loading={captchaVerifying}
                  onClick={handleVerifyCaptcha}
                >
                  验证拼图
                </Button>
              </Space>
            </div>
          )}
          <Button type="primary" htmlType="submit" block size="large" loading={submitting} disabled={!captchaReady}>
            登录并进入系统
          </Button>
        </Form>
      </Card>
    </main>
  );
}
