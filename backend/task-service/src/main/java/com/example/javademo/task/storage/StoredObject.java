package com.example.javademo.task.storage;

import java.io.InputStream;

/**
 * 任务附件对象流。
 *
 * <p>附件下载接口会流式写出 inputStream，避免把较大的附件一次性读入内存。</p>
 */
public record StoredObject(InputStream inputStream, String contentType, long contentLength) {
}
