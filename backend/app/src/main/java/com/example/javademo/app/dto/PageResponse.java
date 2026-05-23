package com.example.javademo.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 通用分页响应 DTO。
 *
 * <p>v0.2 先只在用户分页中使用，但抽成通用结构后，后续审计日志、通知列表、
 * 文件列表等分页接口都可以复用。字段命名尽量贴近 MyBatis Plus Page 的概念，便于对照学习。</p>
 */
@Schema(description = "分页响应")
public class PageResponse<T> {

    /** 当前页码，从 1 开始。 */
    @Schema(description = "当前页码，从 1 开始", example = "1")
    private long current;

    /** 每页条数。 */
    @Schema(description = "每页条数", example = "10")
    private long size;

    /** 总记录数。 */
    @Schema(description = "总记录数", example = "42")
    private long total;

    /** 总页数。 */
    @Schema(description = "总页数", example = "5")
    private long pages;

    /** 当前页数据列表。 */
    @Schema(description = "当前页数据列表")
    private List<T> records;

    public PageResponse() {
    }

    public PageResponse(long current, long size, long total, long pages, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.pages = pages;
        this.records = records;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
