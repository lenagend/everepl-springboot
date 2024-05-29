package com.everepl.evereplspringboot.dto;

import java.time.LocalDateTime;

public record AnnouncementResponse(Long id, String title, String content, LocalDateTime createdAt, LocalDateTime updatedAt, UserResponse user) {}
