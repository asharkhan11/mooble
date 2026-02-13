package in.ashar.mooble.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    public void uploadToMinio(
            InputStream is,
            long size,
            String contentType,
            String objectKey
    ) {

        try {
            // ---------- ensure bucket ----------
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucket)
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucket)
                                .build()
                );
            }

            // ---------- upload object ----------
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(is, size, -1)
                            .contentType(contentType != null
                                    ? contentType
                                    : "application/octet-stream")
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to upload object to MinIO. key=" + objectKey,
                    e
            );
        }
    }

    public void deleteObject(String objectKey) {

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to delete object from MinIO: " + objectKey, e
            );
        }
    }


    public String generatePresignedGetUrl(String objectKey, int expirySeconds) {

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(expirySeconds)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to generate presigned URL for: " + objectKey,
                    e
            );
        }
    }


}
