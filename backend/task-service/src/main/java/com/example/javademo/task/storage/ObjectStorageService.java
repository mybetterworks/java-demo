package com.example.javademo.task.storage;

import java.io.InputStream;

/**
 * task-service 对象存储访问接口。
 *
 * <p>业务层只依赖 put/get 两个动作，集成测试可以 Mock 该接口，不需要启动真实 MinIO。</p>
 */
public interface ObjectStorageService {

    void putObject(String bucket, String objectKey, InputStream inputStream, long objectSize, String contentType);

    StoredObject getObject(String bucket, String objectKey);
}
