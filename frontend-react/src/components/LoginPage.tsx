import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Form, Input, Slider, Space, Typography, message } from 'antd';
import { useState } from 'react';
import { createSliderCaptchaApi, loginApi, verifySliderCaptchaApi } from '../api/backend';
import { ApiError } from '../api/client';
import type { CaptchaRequiredData, LoginResponse, SliderCaptchaChallengeResponse } from '../types';

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
 * 这样登录成功后的状态保存统一交给 App，后续替换登录方式或升级验证码时页面不会变得臃肿。
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

  async function handleFinish(values: LoginFormValues) {
    if (captchaRequired && !captchaToken) {
      /**
       * 高风险状态下不直接提交账号密码，而是先引导用户完成滑块验证。
       * 这样可以避免在验证码缺失时反复触发后端登录接口。
       */
      await loadCaptchaChallenge(values.username);
      messageApi.warning('请先完成滑块验证，再提交登录');
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
         * 前端立刻展示滑块并拉取 challenge，让用户不必猜测下一步该做什么。
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
      setSliderPosition(0);
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

    setCaptchaVerifying(true);
    try {
      /**
       * 当前 MVP 是“滑动到终点”。后端仍按 challengeId 校验一次性状态并返回短 TTL token。
       * 页面只保存 token 到内存，登录成功或重新获取 challenge 后都会清空，不写入 IndexedDB。
       */
      const response = await verifySliderCaptchaApi({
        challengeId: captchaChallenge.challengeId,
        sliderPosition
      });
      setCaptchaToken(response.captchaToken);
      messageApi.success('滑块验证通过，请继续登录');
    } catch (error) {
      setCaptchaToken(null);
      setCaptchaChallenge(null);
      setSliderPosition(0);
      messageApi.error(error instanceof Error ? error.message : '滑块验证失败，请重新获取验证码');
    } finally {
      setCaptchaVerifying(false);
    }
  }

  function handleFormValuesChange(changedValues: Partial<LoginFormValues>) {
    if (changedValues.username !== undefined) {
      /**
       * 验证码 token 和 username + clientIp 绑定。用户名变化后必须丢弃旧 challenge/token，
       * 避免用户误以为旧验证码可以用于新账号。
       */
      resetCaptchaState();
    }
  }

  function resetCaptchaState() {
    setCaptchaRequired(false);
    setCaptchaInfo(null);
    setCaptchaChallenge(null);
    setSliderPosition(0);
    setCaptchaToken(null);
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

  const sliderMax = captchaChallenge ? captchaChallenge.trackWidth - captchaChallenge.puzzleWidth : 100;
  const captchaReady = !captchaRequired || Boolean(captchaToken);

  return (
    <main className="login-scene">
      {contextHolder}
      <section className="login-hero">
        <div className="hero-kicker">v0.3 React Admin</div>
        <Typography.Title level={1}>把微服务学习做成可触摸的系统</Typography.Title>
        <Typography.Paragraph>
          这一版先让浏览器真正接入后端登录和用户管理 API。后面接网关、缓存、消息和观测时，这个前端会成为稳定的验证入口。
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
                message={captchaToken ? '滑块验证已通过' : '需要滑块验证'}
                description={
                  captchaInfo
                    ? `当前 5 分钟内失败 ${captchaInfo.failureCount}/${captchaInfo.failureThreshold} 次，请完成验证后继续登录。`
                    : '登录失败次数较多，请完成验证后继续登录。'
                }
              />
              <div className="captcha-track">
                <div className="captcha-track-copy">
                  {captchaChallenge?.instruction ?? '点击获取验证码后，将滑块拖动到最右侧'}
                </div>
                <Slider
                  min={0}
                  max={sliderMax}
                  value={sliderPosition}
                  disabled={!captchaChallenge || Boolean(captchaToken)}
                  tooltip={{ formatter: (value) => `${value ?? 0}px` }}
                  onChange={(value) => setSliderPosition(Array.isArray(value) ? value[0] : value)}
                />
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
                  验证滑块
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
