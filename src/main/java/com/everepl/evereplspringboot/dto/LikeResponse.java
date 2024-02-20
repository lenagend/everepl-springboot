package com.everepl.evereplspringboot.dto;

import com.everepl.evereplspringboot.entity.Target;

public record LikeResponse(
        Long id,
        Long targetId,
        Target.TargetType type
) {}