package com.everepl.evereplspringboot.dto;

import com.everepl.evereplspringboot.entity.Target;

public record CommentRequest(
    String nickname,
    String text,
    String password,
    Long targetId,
    Target.TargetType type,
    Boolean isDeleted
){}
