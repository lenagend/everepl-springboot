package com.everepl.evereplspringboot.controller;

import com.everepl.evereplspringboot.dto.LikeRequest;
import com.everepl.evereplspringboot.dto.LikeResponse;
import com.everepl.evereplspringboot.service.UserLikeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/like")
public class UserLikeController {
    private final UserLikeService userLikeService;

    public UserLikeController(UserLikeService userLikeService) {
        this.userLikeService = userLikeService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addLike(@Validated @RequestBody LikeRequest likeRequest) {
        LikeResponse savedLike = userLikeService.addLike(likeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedLike);
    }

    @GetMapping
    public ResponseEntity<?> getUserLikes(@Validated @RequestBody LikeRequest likeRequest, Pageable pageable) {
        Page<?> likes = userLikeService.processUserLikes(likeRequest, pageable);
        return ResponseEntity.ok(likes);
    }

}
