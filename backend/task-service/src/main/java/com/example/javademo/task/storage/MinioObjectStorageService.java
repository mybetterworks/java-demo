package com.example.javademo.task.storage;

import com.example.javademo.task.common.BusinessException;
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
 * task-service 的 MinIO 对象存储实现。
 *
 * <p>任务附件和任务元数据分离保存：文件内容进 MinIO，文件名、大小、contentType 和 objectKey 进 MySQL。
 * 这样可以避免数据库 BLOB 膨胀，也保留了后续按任务查询附件列表的关系型能力。</p>
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
            ensureBucket(bucket);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(inputStream, objectSize, -1)
                    .contentType(contentType)
                    .build());
            log.info("Task object uploaded to MinIO, bucket={}, objectKey={}, sizeBytes={}, contentType={}",
                    bucket, objectKey, objectSize, contentType);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("Task MinIO object upload failed, bucket={}, objectKey={}, reason={}",
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
            log.warn("Task MinIO object read failed, bucket={}, objectKey={}, reason={}",
                    bucket, objectKey, exception.getClass().getSimpleName());
            throw BusinessException.notFound("Stored object does not exist");
        }
    }

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
                log.info("Task MinIO bucket created, bucket={}", bucket);
            }
            initializedBuckets.add(bucket);
        }
    }
}
