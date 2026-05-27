package com.example.javademo.task.client;

/**
 * 用户服务返回的最小用户资料。
 *
 * <p>任务服务当前只关心用户是否存在以及用户 ID，因此只定义需要读取的字段。这样即使 java-demo-app
 * 响应中包含 nickname、role 等更多字段，也不会和任务服务产生不必要的耦合。</p>
 */
public class UserProfileResponse {

    private Long id;
    private String username;
    private Integer status;

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
