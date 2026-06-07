package com.example.javademo.app.storage;

import com.example.javademo.app.common.BusinessException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 MinIO SDK 的对象存储实现。
 *
 * <p>v0.9 只需要单节点 MinIO，但仍按 S3 兼容对象存储的方式组织：
 * bucket 代表业务域，objectKey 代表对象路径，数据库只保存 key 和业务元数据。
 * 首次写入某个 bucket 前会懒检查并创建 bucket，方便本地环境空数据卷直接启动。</p>
 */
@Service
public class MinioObjectStorageService implements ObjectStorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioObjectStorageService.class);

    private final MinioClient minioClient;
    private final MinioProperties properties;
    private final Set<String> initializedBuckets = ConcurrentHashMap.newKeySet();

    public MinioObjectStorageService(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @Override
    public void putObject(String bucket, String objectKey, InputStream inputStream, long objectSize, String contentType) {
        if (!properties.isEnabled()) {
            throw BusinessException.storageUnavailable("Object storage is disabled");
        }
        try {
            // bucket 初始化放在上传前，保证本地首次启动时不需要手动登录控制台创建 bucket。
            ensureBucket(bucket);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(inputStream, objectSize, -1)
                    .contentType(contentType)
                    .build());
            log.info("Object uploaded to MinIO, bucket={}, objectKey={}, sizeBytes={}, contentType={}",
                    bucket, objectKey, objectSize, contentType);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("MinIO object upload failed, bucket={}, objectKey={}, reason={}",
                    bucket, objectKey, exception.getClass().getSimpleName());
            throw BusinessException.storageUnavailable("Object storage is unavailable");
        }
    }

    @Override
    public StoredObject getObject(String bucket, String objectKey) {
        if (!properties.isEnabled()) {
            throw BusinessException.storageUnavailable("Object storage is disabled");
        }
        try {
            // 先读取 stat 是为了给 HTTP 响应补充 contentType/contentLength。
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
            return new StoredObject(inputStream, stat.contentType(), stat.size());
        } catch (Exception exception) {
            log.warn("MinIO object read failed, bucket={}, objectKey={}, reason={}",
                    bucket, objectKey, exception.getClass().getSimpleName());
            throw BusinessException.notFound("Stored object does not exist");
        }
    }

    /**
     * 懒初始化 bucket。
     *
     * <p>initializedBuckets 只做本进程优化，避免每次上传都请求 MinIO。
     * 即使多实例同时首次上传，MinIO 的 bucketExists/makeBucket 组合也能保证最终 bucket 可用。</p>
     */
    private void ensureBucket(String bucket) throws Exception {
        if (initializedBuckets.contains(bucket)) {
            return;
        }
        synchronized (initializedBuckets) {
            if (initializedBuckets.contains(bucket)) {
                return;
            }
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("MinIO bucket created, bucket={}", bucket);
            }
            initializedBuckets.add(bucket);
        }
    }
}
