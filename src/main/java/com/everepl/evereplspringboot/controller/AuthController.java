package com.everepl.evereplspringboot.controller;

import com.everepl.evereplspringboot.dto.UserRequest;
import com.everepl.evereplspringboot.dto.UserResponse;
import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.service.FileStorageService;
import com.everepl.evereplspringboot.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    public AuthController(UserService userService, FileStorageService fileStorageService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String token) {
            User user = userService.verifyTokenAndFetchUser(token);
            return ResponseEntity.ok().body("Token is valid and user is " + user.getName());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id){
        UserResponse user = userService.getUserByUserId(id);
        return ResponseEntity.ok(user);
    }

    @PatchMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateUser(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "notificationSetting", required = false) Boolean notificationSetting,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        // UserRequest 객체 생성
        UserRequest userRequest = new UserRequest(name, imageUrl, notificationSetting);

        // 이미지 파일이 있을 경우 처리
        if (profileImage != null && !profileImage.isEmpty()) {
            String imagePath = fileStorageService.store(profileImage, "image");
            userRequest.setImageUrl(imagePath); // userRequest에 이미지 URL 추가
        }

        UserResponse userResponse = userService.updateUser(userRequest);
        return ResponseEntity.ok(userResponse);
    }

}
