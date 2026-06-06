package com.example.javademo.rpc;

/**
 * Dubbo 附件常量。
 *
 * <p>v0.6.2 只把用户校验这条链路改为 Dubbo，因此当前公共附件也刻意保持最小集合，
 * 仅同步跨服务日志串联真正需要的 requestId。这样既能保留 v0.5.2 建立起来的日志观测能力，
 * 也不会为了一个学习型版本过早引入过多跨服务上下文字段。</p>
 */
public final class DubboAttachmentConstants {

    /** 透传到 Dubbo provider 的 requestId 附件键。 */
    public static final String REQUEST_ID = "java-demo-request-id";

    private DubboAttachmentConstants() {
    }
}
