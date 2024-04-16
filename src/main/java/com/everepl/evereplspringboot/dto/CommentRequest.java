package com.everepl.evereplspringboot.dto;

import com.everepl.evereplspringboot.dto.validation.CreateGroup;
import com.everepl.evereplspringboot.dto.validation.ReadGroup;
import com.everepl.evereplspringboot.dto.validation.UpdateGroup;
import com.everepl.evereplspringboot.entity.Target;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentRequest(
    @NotNull(groups = {CreateGroup.class, UpdateGroup.class}, message = "내용이 입력되지 않았습니다...")
    @Size(max = 5000, message = "댓글은 최대 5000자까지 입력 가능합니다.")
    String text,

    @NotNull(groups = {CreateGroup.class, UpdateGroup.class, ReadGroup.class})
    Long targetId,

    @NotNull(groups = {CreateGroup.class, UpdateGroup.class, ReadGroup.class})
    Target.TargetType type
){}
