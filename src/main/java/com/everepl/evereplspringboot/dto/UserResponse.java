package com.everepl.evereplspringboot.dto;


import java.util.Set;

public record UserResponse(
        Long id,
        String name,
        String imageUrl,
        String provider,
        Set<String> roles,
        Boolean notificationSetting
) {}
