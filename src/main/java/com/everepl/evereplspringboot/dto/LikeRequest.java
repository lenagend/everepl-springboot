package com.everepl.evereplspringboot.dto;

import com.everepl.evereplspringboot.dto.validation.CreateGroup;
import com.everepl.evereplspringboot.dto.validation.UpdateGroup;
import com.everepl.evereplspringboot.entity.Target;
import jakarta.validation.constraints.NotNull;

public record LikeRequest(
        @NotNull(groups = UpdateGroup.class)
        Long targetId,
        @NotNull
        Target.TargetType type
) {}