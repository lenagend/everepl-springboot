package com.everepl.evereplspringboot.dto;

import com.everepl.evereplspringboot.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String nickname,
        String text,
        Long targetId,
        Comment.targetType type,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer commentCount,
        Integer likeCount,
        Integer reportCount,
        Double popularityScore
) {}