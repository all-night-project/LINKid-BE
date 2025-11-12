package com.example.linkid.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class ObjectStorageService {

    private final AmazonS3 amazonS3;

    @Value("${ncloud.object-storage.bucket-name}")
    private String bucketName;

    /**
     * Presigned URL 생성 (업로드용)
     */
    public String generatePresignedUploadUrl(String bucketKey, String contentType) {
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 15); // 15분

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, bucketKey)
                .withMethod(HttpMethod.PUT)
                .withExpiration(expiration)
                .withContentType(contentType);

        URL url = amazonS3.generatePresignedUrl(request);
        return url.toString();
    }

    /**
     * Object URL 생성 (공개 접근 가능한 URL)
     */
    public String getObjectUrl(String bucketKey) {
        return amazonS3.getUrl(bucketName, bucketKey).toString();
    }
}