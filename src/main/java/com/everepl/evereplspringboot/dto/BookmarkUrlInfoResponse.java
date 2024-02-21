package com.everepl.evereplspringboot.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookmarkUrlInfoResponse(
        Long id,
        String url,
        String title,
        String faviconSrc,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDate updateDate,
        Integer viewCount,
        Integer commentCount,
        Integer likeCount,
        Integer reportCount
) implements BookmarkResponse{}