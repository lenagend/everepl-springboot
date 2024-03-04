package com.everepl.evereplspringboot.dto;

import com.everepl.evereplspringboot.entity.Target;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String userIp,
        String nickname,
        String text,
        Long targetId,
        Target.TargetType type,
        String parentCommentNickname,
        String parentCommentUserIp,
        String path,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean isDeleted,
        Integer commentCount,
        Integer likeCount,
        Integer reportCount
) {}
