package com.everepl.evereplspringboot.dto;

public record UserRequest(
        String name,
        String imageUrl,
        Boolean notificationSetting
) {}
