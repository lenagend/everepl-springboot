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
        List<CommentResponse> replies, // 변경된 부분
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer commentCount,
        Integer likeCount,
        Integer reportCount
) {}
