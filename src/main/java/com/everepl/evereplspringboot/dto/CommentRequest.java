package com.everepl.evereplspringboot.dto;

import com.everepl.evereplspringboot.dto.validation.CreateGroup;
import com.everepl.evereplspringboot.dto.validation.UpdateGroup;
import com.everepl.evereplspringboot.entity.Target;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentRequest(

    @NotNull(groups = {CreateGroup.class, UpdateGroup.class},  message = "닉네임이 입력되지 않았습니다...")
    @Size(min = 2, max = 8, message = "닉네임은 2~8글자 사이여야 합니다.")
    String nickname,

    @NotNull(groups = {CreateGroup.class, UpdateGroup.class}, message = "내용이 입력되지 않았습니다...")
    @Size(max = 5000, message = "댓글은 최대 5000자까지 입력 가능합니다.")
    String text,

    @NotNull(groups = {CreateGroup.class, UpdateGroup.class}, message = "비밀번호가 입력되지 않았습니다...")
    @Size(min = 4, max = 15, message = "비밀번호는 4~15 사이로만 입력 가능합니다. ")
    String password,

    @NotNull
    Long targetId,

    @NotNull
    Target.TargetType type,

    Boolean isDeleted
){}
