package com.everepl.evereplspringboot.dto;

import com.everepl.evereplspringboot.entity.Target;
import jakarta.validation.constraints.NotNull;

public record LikeRequest(
        @NotNull
        Long targetId,
        @NotNull
        Target.TargetType type
) {}