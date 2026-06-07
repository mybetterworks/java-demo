package com.example.javademo.app.storage;

import java.io.InputStream;

/**
 * 从对象存储读取出的对象流。
 *
 * <p>Controller 会把 inputStream 直接交给 Spring MVC 写回响应，避免一次性把文件读入内存。
 * contentType 和 contentLength 用于设置响应头，便于浏览器正确展示图片或下载文件。</p>
 */
public record StoredObject(InputStream inputStream, String contentType, long contentLength) {
}
