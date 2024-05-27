package com.everepl.evereplspringboot.dto;

import com.everepl.evereplspringboot.entity.User;

public record UserResponse(
        Long id,
        String name,
        String imageUrl,
        String provider,
        User.Role role,
        Boolean notificationSetting
) {}
