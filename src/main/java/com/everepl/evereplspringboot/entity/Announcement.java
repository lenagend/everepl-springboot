package com.everepl.evereplspringboot.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 20000) // 데이터베이스 컬럼 길이도 설정
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        LocalDateTime now = LocalDateTime.now();
        updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        if (createdAt != null) {
            return createdAt.withSecond(0).withNano(0);
        }
        return null;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        if (updatedAt != null) {
            return updatedAt.withSecond(0).withNano(0);
        }
        return null;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}