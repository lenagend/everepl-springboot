package com.everepl.evereplspringboot.dto;

import com.everepl.evereplspringboot.entity.Comment;

public record CommentRequest(
    String nickname,
    String text,
    String password,
    Long targetId,
    Comment.targetType type
){}
