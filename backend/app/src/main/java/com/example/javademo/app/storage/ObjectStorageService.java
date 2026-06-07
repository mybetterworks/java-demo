package com.example.javademo.app.storage;

import java.io.InputStream;

/**
 * 对象存储访问接口。
 *
 * <p>业务层不直接依赖 MinIO SDK，而是依赖这个最小接口。
 * 这样集成测试可以用 MockBean 替换对象存储，后续也可以把实现替换为其他 S3 兼容服务。</p>
 */
public interface ObjectStorageService {

    /**
     * 写入对象。
     *
     * @param bucket      bucket 名称
     * @param objectKey   对象 key
     * @param inputStream 文件输入流
     * @param objectSize  文件字节数
     * @param contentType MIME 类型
     */
    void putObject(String bucket, String objectKey, InputStream inputStream, long objectSize, String contentType);

    /**
     * 读取对象。
     *
     * @param bucket    bucket 名称
     * @param objectKey 对象 key
     * @return 对象流和响应元数据
     */
    StoredObject getObject(String bucket, String objectKey);
}
