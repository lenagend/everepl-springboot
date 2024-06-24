package com.everepl.evereplspringboot.controller;

import com.everepl.evereplspringboot.dto.AnnouncementRequest;
import com.everepl.evereplspringboot.dto.AnnouncementResponse;
import com.everepl.evereplspringboot.dto.UserRequest;
import com.everepl.evereplspringboot.dto.UserResponse;
import com.everepl.evereplspringboot.service.AnnouncementService;
import com.everepl.evereplspringboot.service.S3StorageService;
import com.everepl.evereplspringboot.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AnnouncementService announcementService;

    private final UserService userService;

    private final S3StorageService s3StorageService;

    public AdminController(AnnouncementService announcementService, UserService userService, S3StorageService s3StorageService) {
        this.announcementService = announcementService;
        this.userService = userService;
        this.s3StorageService = s3StorageService;
    }

    @PostMapping("/announcements")
    public ResponseEntity<AnnouncementResponse> createAnnouncement(@RequestBody AnnouncementRequest request) {
        AnnouncementResponse response = announcementService.createAnnouncement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/announcements/{id}")
    public ResponseEntity<AnnouncementResponse> updateAnnouncement(@PathVariable Long id, @RequestBody AnnouncementRequest request) {
        AnnouncementResponse response = announcementService.updateAnnouncement(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/announcements/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> usersPage = userService.getAllUsers(pageable);
        return ResponseEntity.ok(usersPage);
    }

    @PatchMapping(value = "/users/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        // UserRequest 객체 생성
        UserRequest userRequest = new UserRequest(name, imageUrl, null);

        // 이미지 파일이 있을 경우 처리
        if (profileImage != null && !profileImage.isEmpty()) {
            String imagePath = s3StorageService.store(profileImage, "image");
            userRequest.setImageUrl(imagePath); // userRequest에 이미지 URL 추가
        }

        UserResponse updatedUser = userService.updateUserByAdmin(id, userRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/suspend-profile-picture")
    public ResponseEntity<Void> suspendProfilePicture(
            @PathVariable Long id,
            @RequestParam int days
    ) {
        userService.suspendProfilePicture(id, days);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/suspend-comments")
    public ResponseEntity<Void> suspendComments(
            @PathVariable Long id,
            @RequestParam int days
    ) {
        userService.suspendComments(id, days);
        return ResponseEntity.noContent().build();
    }
}