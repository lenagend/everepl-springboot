package com.everepl.evereplspringboot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

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
                .httpClient(ApacheHttpClient.builder().build()) // ApacheHttpClient 사용
                .build();
    }

    public String store(MultipartFile file, String type) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("빈 파일을 저장할 수 없습니다.");
            }

            long maxFileSize = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > maxFileSize) {
                throw new RuntimeException("파일 크기가 너무 큽니다. 최대 5MB까지 허용됩니다.");
            }

            // 파일명에 고유 식별자를 추가하여 중복을 방지
            String originalFilename = file.getOriginalFilename();
            String filename = UUID.randomUUID() + "-" + (originalFilename != null ? originalFilename : "file");

            // S3 버킷에 저장할 경로 설정
            String key = getSubDirectory(type) + "/" + filename;

            // 파일의 MIME 타입을 결정
            String contentType = determineContentType(originalFilename);

            // S3에 파일 업로드, Content-Type 설정 추가
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType) // Content-Type 설정
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            // 업로드한 파일의 URL 반환
            return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(key)).toExternalForm();
        } catch (S3Exception e) {
            throw new RuntimeException("S3와 통신 중 오류가 발생했습니다", e);
        } catch (IOException e) {
            throw new RuntimeException("파일 읽기 중 오류가 발생했습니다", e);
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

    // 파일명의 확장자를 기반으로 MIME 타입을 결정하는 메소드
    private String determineContentType(String filename) {
        if (filename == null) {
            return "application/octet-stream"; // 기본 MIME 타입
        }
        // 파일 확장자를 기반으로 Content-Type 결정 (예: image/jpeg, image/png 등)
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.endsWith(".png")) {
            return "image/png";
        } else if (filename.endsWith(".gif")) {
            return "image/gif";
        } else if (filename.endsWith(".pdf")) {
            return "application/pdf";
        }
        // 기타 기본 Content-Type 설정
        return "application/octet-stream";
    }

    // 파일명을 정규화하는 메소드
    private String sanitizeFilename(String filename) {
        if (filename == null) return null;
        return filename.replaceAll("[^a-zA-Z0-9.-]", "_"); // 파일명에서 유효하지 않은 문자를 '_'로 대체
    }
}