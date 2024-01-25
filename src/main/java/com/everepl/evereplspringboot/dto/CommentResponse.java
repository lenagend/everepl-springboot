package com.everepl.evereplspringboot.dto;

import com.everepl.evereplspringboot.entity.Comment;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Long id,
        String userIp,
        String nickname,
        String text,
        Long targetId,
        Comment.targetType type,
        String path,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean isDeleted,
        Integer commentCount,
        Integer likeCount,
        Integer reportCount
) {}
