package com.everepl.evereplspringboot.controller;

import com.everepl.evereplspringboot.dto.LikeRequest;
import com.everepl.evereplspringboot.dto.LikeResponse;
import com.everepl.evereplspringboot.service.UserLikeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/like")
public class UserLikeController {
    private final UserLikeService userLikeService;

    public UserLikeController(UserLikeService userLikeService) {
        this.userLikeService = userLikeService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addLike(HttpServletRequest request, @Validated @RequestBody LikeRequest likeRequest) {
        String userIp = request.getRemoteAddr();
        LikeResponse savedLike = userLikeService.addLike(likeRequest, userIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedLike);
    }


}
