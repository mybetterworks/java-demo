package com.example.javademo.rpc.user;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户 RPC 摘要响应。
 *
 * <p>任务服务当前只关心负责人用户是否存在，以及最小可追踪字段，
 * 因此这里不暴露 passwordHash、lastLoginAt 等与任务校验无关的内部信息。</p>
 */
public class UserSummaryRpcResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户 ID。 */
    private Long id;

    /** 用户名，便于日志和未来任务详情扩展使用。 */
    private String username;

    /** 用户状态；v0.6.2 仅透传，不额外改变现有“存在且未删除即可”的业务语义。 */
    private Integer status;

    public UserSummaryRpcResponse() {
    }

    public UserSummaryRpcResponse(Long id, String username, Integer status) {
        this.id = id;
        this.username = username;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
