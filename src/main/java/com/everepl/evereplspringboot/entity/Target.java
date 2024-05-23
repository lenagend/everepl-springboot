package com.everepl.evereplspringboot.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class Target {
    private Long targetId;

    @Enumerated(EnumType.STRING)
    private TargetType type;

    public enum TargetType {
        URLINFO, COMMENT, USER // 추후 다른 타입 추가 가능
    }

    // 기본 생성자
    public Target() {
    }

    public Target(Long targetId, TargetType type) {
        this.targetId = targetId;
        this.type = type;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public TargetType getType() {
        return type;
    }

    public void setType(TargetType type) {
        this.type = type;
    }
}
