package com.everepl.evereplspringboot.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class UserLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userIp;

    @Embedded
    private Target target;

    @Column(nullable = false)
    private LocalDate likedDate;

    @PrePersist
    protected void onCreate() {
        this.likedDate = LocalDate.now(); // 엔티티가 저장되기 전에 현재 날짜로 likedDate 초기화
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public LocalDate getLikedDate() {
        return likedDate;
    }

    public void setLikedDate(LocalDate likedDate) {
        this.likedDate = likedDate;
    }
}
