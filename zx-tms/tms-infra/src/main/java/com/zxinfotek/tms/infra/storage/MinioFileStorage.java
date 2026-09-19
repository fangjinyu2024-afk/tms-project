package com.zxinfotek.tms.infra.storage;

import com.zxinfotek.tms.common.exception.BizException;
import com.zxinfotek.tms.common.exception.CommonErrorCode;
import com.zxinfotek.tms.common.util.UtcTimes;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Component
public class MinioFileStorage implements FileStorage {

    private static final Logger log = LoggerFactory.getLogger(MinioFileStorage.class);
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyyMM");

    private final StorageProperties properties;
    private volatile MinioClient client;
    private volatile boolean bucketReady;

    public MinioFileStorage(StorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public String upload(String objectKey, byte[] content, String contentType) {
        try {
            ensureBucket();
            client().putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .stream(new ByteArrayInputStream(content), content.length, -1)
                    .contentType(contentType)
                    .build());
            return objectKey;
        } catch (Exception e) {
            log.error("对象存储写入失败，objectKey={}", objectKey, e);
            throw new BizException(CommonErrorCode.COMMON_004, "msg.infra.storageUnavailable");
        }
    }

    @Override
    public InputStream download(String objectKey) {
        try {
            return client().getObject(GetObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            log.error("对象存储读取失败，objectKey={}", objectKey, e);
            throw new BizException(CommonErrorCode.COMMON_004, "msg.infra.storageUnavailable");
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            client().removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            log.error("对象存储删除失败，objectKey={}", objectKey, e);
            throw new BizException(CommonErrorCode.COMMON_004, "msg.infra.storageUnavailable");
        }
    }

    @Override
    public String presignedUrl(String objectKey, Duration ttl) {
        try {
            return client().getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .expiry((int) ttl.getSeconds(), TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            log.error("生成预签名链接失败，objectKey={}", objectKey, e);
            throw new BizException(CommonErrorCode.COMMON_004, "msg.infra.storageUnavailable");
        }
    }

    @Override
    public String buildObjectKey(String type, Long tenantId, String objectId, String extension) {
        return type + "/" + (tenantId == null ? 0L : tenantId) + "/"
                + MONTH.format(UtcTimes.now()) + "/" + objectId + extension;
    }

    private MinioClient client() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    if (properties.getEndpoint() == null || properties.getEndpoint().isBlank()) {
                        throw new BizException(CommonErrorCode.COMMON_004, "msg.infra.storageNotConfigured");
                    }
                    client = MinioClient.builder()
                            .endpoint(properties.getEndpoint())
                            .credentials(properties.getAccessKey(), properties.getSecretKey())
                            .build();
                }
            }
        }
        return client;
    }

    private void ensureBucket() throws Exception {
        if (bucketReady) {
            return;
        }
        synchronized (this) {
            if (bucketReady) {
                return;
            }
            boolean exists = client().bucketExists(BucketExistsArgs.builder()
                    .bucket(properties.getBucket()).build());
            if (!exists) {
                client().makeBucket(MakeBucketArgs.builder().bucket(properties.getBucket()).build());
            }
            bucketReady = true;
        }
    }
}
