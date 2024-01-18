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
    private String userIp;

    @NotNull(message = "닉네임이 입력되지 않았습니다...")
    @Size(min = 2, max = 8, message = "닉네임은 2~8글자 사이여야 합니다.")
    private String nickname;

    @NotNull(message = "내용이 입력되지 않았습니다...")
    private String text;

    @NotNull(message = "비밀번호가 입력되지 않았습니다...")
    private String password;

    private Long targetId;

    @Enumerated(EnumType.STRING)
    private targetType type;
    public enum targetType { //추후 BOARD 등 추가.
        URLINFO
    }

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id") // 부모 댓글의 ID를 참조하는 외래 키
    private Comment parentComment;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long parrentId) {
        this.targetId = parrentId;
    }

    public targetType getType() {
        return type;
    }

    public void setType(targetType type) {
        this.type = type;
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

    public void incrementCommentCount(){
        this.commentCount += 1;
    }

    public void decrementCommentCount(){
        if(this.commentCount > 0){
            this.commentCount -= 1;
        }
    }

    public void incrementReportCount(){
        this.reportCount += 1;
    }
}
