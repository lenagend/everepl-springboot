package com.everepl.evereplspringboot.dto;

public record UserResponse(
        Long id,
        String name,
        String imageUrl,
        String provider,
        Boolean notificationSetting
) {}
