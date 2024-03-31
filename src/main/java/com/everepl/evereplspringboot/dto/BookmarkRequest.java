package com.everepl.evereplspringboot.dto;

import com.everepl.evereplspringboot.entity.Target;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BookmarkRequest(
        @NotNull
        Target.TargetType targetType,
        @NotNull
        List<Long> targetIds
) {}
