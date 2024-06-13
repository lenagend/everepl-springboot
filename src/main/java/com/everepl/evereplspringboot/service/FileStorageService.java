package com.everepl.evereplspringboot.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
//
//@Service
//public class FileStorageService {
//
//    private final Path rootLocation;
//
//    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) throws IOException {
//        this.rootLocation = Paths.get(uploadDir);
//        // 기본 루트 디렉토리 생성 (필요한 경우)
//        Files.createDirectories(this.rootLocation);
//    }
//
//    public String store(MultipartFile file, String type) {
//        try {
//            if (file.isEmpty()) {
//                throw new RuntimeException("Failed to store empty file.");
//            }
//
//            // 파일 유형에 따라 서브 디렉토리 결정
//            String subDirectory = getSubDirectory(type);
//            Path typeSpecificLocation = this.rootLocation.resolve(subDirectory);
//            Files.createDirectories(typeSpecificLocation);  // 해당 디렉토리가 없으면 생성
//
//            // 파일명에 고유 식별자를 추가하여 중복을 방지
//            String originalFilename = file.getOriginalFilename();
//            String filename = UUID.randomUUID() + "-" + (originalFilename != null ? originalFilename : "file");
//
//            // 최종 저장 경로 계산
//            Path destinationFile = typeSpecificLocation.resolve(Paths.get(filename)).normalize().toAbsolutePath();
//            if (!destinationFile.getParent().equals(typeSpecificLocation.toAbsolutePath())) {
//                // 상위 디렉토리로 이동하는 경로를 방지
//                throw new RuntimeException("Cannot store file outside current directory.");
//            }
//
//            // 파일 저장
//            Files.copy(file.getInputStream(), destinationFile);
//
//            // 상대 경로 반환
//            return Paths.get(subDirectory).resolve(filename).toString();
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to store file.", e);
//        }
//    }
//
//    // 파일 유형에 따라 서브 디렉토리를 반환하는 메소드
//    private String getSubDirectory(String type) {
//        switch (type) {
//            case "image":
//                return "images";
//            case "document":
//                return "documents";
//            case "video":
//                return "videos";
//            default:
//                return "others";
//        }
//    }
//}
