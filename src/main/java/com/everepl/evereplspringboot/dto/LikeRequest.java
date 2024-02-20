package com.everepl.evereplspringboot.dto;

import com.everepl.evereplspringboot.entity.Target;

public record LikeRequest(
        Long targetId,
        Target.TargetType type
) {}