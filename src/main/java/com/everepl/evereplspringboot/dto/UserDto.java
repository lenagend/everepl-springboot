package com.everepl.evereplspringboot.dto;

public record UserDto(
        Long id,
        String userName,
        String imageUrl
) {}
