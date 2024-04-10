package com.everepl.evereplspringboot.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class UserLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 외래키로 사용할 컬럼명을 지정합니다.
    private User user;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
