package com.everepl.evereplspringboot.controller;

import com.everepl.evereplspringboot.dto.CommentRequest;
import com.everepl.evereplspringboot.dto.CommentResponse;
import com.everepl.evereplspringboot.dto.UserRequest;
import com.everepl.evereplspringboot.dto.UserResponse;
import com.everepl.evereplspringboot.dto.validation.UpdateGroup;
import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
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

    @PatchMapping
    public ResponseEntity<?> updateUser(
            @Validated @RequestBody UserRequest userRequest) {
        UserResponse userResponse = userService.updateUser(userRequest);
        return ResponseEntity.ok(userResponse);
    }
}
