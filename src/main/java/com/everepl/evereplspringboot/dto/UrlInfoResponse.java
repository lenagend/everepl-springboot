package com.everepl.evereplspringboot.dto;

import java.time.LocalDateTime;

public record UrlInfoResponse(
        Long id,
        String url,
        String title,
        String faviconSrc,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer viewCount,
        Integer commentCount,
        Integer reportCount,
        String lastComment
) {}