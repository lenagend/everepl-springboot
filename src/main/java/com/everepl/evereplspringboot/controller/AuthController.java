package com.everepl.evereplspringboot.controller;

import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
}
