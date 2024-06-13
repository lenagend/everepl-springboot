package com.everepl.evereplspringboot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final String bucketName;

    public S3StorageService(
            @Value("${aws.access-key-id}") String accessKeyId,
            @Value("${aws.secret-access-key}") String secretAccessKey,
            @Value("${aws.region}") String region,
            @Value("${aws.s3.bucket-name}") String bucketName) {
        this.bucketName = bucketName;
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .httpClientBuilder(UrlConnectionHttpClient.builder()) // HTTP 클라이언트 설정
                .build();
    }

    public String store(MultipartFile file, String type) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("빈 파일을 저장할 수 없습니다.");
            }

            // 파일명에 고유 식별자를 추가하여 중복을 방지
            String originalFilename = file.getOriginalFilename();
            String filename = UUID.randomUUID() + "-" + (originalFilename != null ? originalFilename : "file");

            // S3 버킷에 저장할 경로 설정
            String key = getSubDirectory(type) + "/" + filename;

            // S3에 파일 업로드, 퍼블릭 읽기 권한 설정
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .acl(ObjectCannedACL.PUBLIC_READ) // 퍼블릭 읽기 권한 설정
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            // 업로드한 파일의 URL 반환
            return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(key)).toExternalForm();
        } catch (IOException e) {
            throw new RuntimeException("S3에 저장하는 것을 실패했습니다", e);
        }
    }

    // 파일 유형에 따라 서브 디렉토리를 반환하는 메소드
    private String getSubDirectory(String type) {
        switch (type) {
            case "image":
                return "images";
            case "document":
                return "documents";
            case "video":
                return "videos";
            default:
                return "others";
        }
    }
}
