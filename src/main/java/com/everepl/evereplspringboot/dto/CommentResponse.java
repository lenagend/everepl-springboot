package com.everepl.evereplspringboot.dto;

import com.everepl.evereplspringboot.entity.Target;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        CommentUserDto user,
        String text,
        Long targetId,
        Target.TargetType type,
        CommentUserDto parentCommentUser,
        String path,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean isDeleted,
        Boolean isModified,
        Integer commentCount,
        Integer likeCount,
        Integer reportCount,
        String rootUrl
) {}