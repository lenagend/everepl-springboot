package com.everepl.evereplspringboot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StaticResourceController {
    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> handleFavicon() {
        return ResponseEntity.noContent().build();
    }
}
