package com.everepl.evereplspringboot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 5000) // 데이터베이스 컬럼 길이도 설정
    private String text;

    @Embedded
    private Target target;

    // Materialized Path를 활용할 때는 이 필드를 쿼리 로직에서 사용하지 않음
    @OneToMany(mappedBy = "parentComment", fetch = FetchType.LAZY)
    private List<Comment> replies = new ArrayList<>();

    // 부모 댓글의 참조. Materialized Path와 함께 사용될 수 있지만, 필수는 아님
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id") // 부모 댓글의 ID를 참조하는 외래 키
    private Comment parentComment;

    private String path;

    // rootTargetType 필드 추가
    @Enumerated(EnumType.STRING)
    private Target.TargetType rootTargetType;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean isDeleted = false;

    private Integer commentCount = 0;
    private Integer likeCount = 0;
    private Integer reportCount = 0;

    private Double popularityScore = 0.0;

    public Comment() {
    }

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public List<Comment> getReplies() {
        return replies;
    }

    public void setReplies(List<Comment> replies) {
        this.replies = replies;
    }

    public Comment getParentComment() {
        return parentComment;
    }

    public void setParentComment(Comment parentComment) {
        this.parentComment = parentComment;
    }

    public String getPath() {
        return path;
    }

    public Target.TargetType getRootTargetType() {
        return rootTargetType;
    }

    public void setRootTargetType(Target.TargetType rootTargetType) {
        this.rootTargetType = rootTargetType;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getReportCount() {
        return reportCount;
    }

    public void setReportCount(Integer reportCount) {
        this.reportCount = reportCount;
    }

    public Double getPopularityScore() {
        return popularityScore;
    }

    public void setPopularityScore(Double popularityScore) {
        this.popularityScore = popularityScore;
    }

    public void incrementLikeCount(){
        this.likeCount += 1;
    }

    public void updateCommentCount(int increment) {
        this.commentCount += increment;
    }

    public void incrementReportCount(){
        this.reportCount += 1;
    }
}
