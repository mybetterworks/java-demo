package com.example.javademo.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 通用分页响应。
 *
 * @param <T> 分页记录类型
 */
@Schema(description = "分页响应")
public class PageResponse<T> {

    private long current;
    private long size;
    private long total;
    private long pages;
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
