package com.everepl.evereplspringboot.dto;

import com.everepl.evereplspringboot.entity.Target;

import java.util.List;

public record BookmarkRequest(Target.TargetType targetType, List<Long> targetIds) {}
