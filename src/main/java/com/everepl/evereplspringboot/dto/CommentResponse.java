package com.everepl.evereplspringboot.dto;

import com.everepl.evereplspringboot.entity.Target;

import java.time.LocalDateTime;
import java.util.List;

public class CommentResponse {
    private Long id;
    private CommentUserResponse user;
    private String text;
    private Long targetId;
    private Target.TargetType type;
    private List<CommentResponse> replies;
    private CommentUserResponse parentCommentUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
    private boolean isModified;
    private Integer commentCount;
    private Integer likeCount;
    private Integer reportCount;
    private String link;

    public CommentResponse(Long id, CommentUserResponse user, String text, Long targetId, Target.TargetType type,
                           CommentUserResponse parentCommentUser, LocalDateTime createdAt, LocalDateTime updatedAt,
                           boolean isDeleted, boolean isModified, Integer commentCount, Integer likeCount, Integer reportCount) {
        this.id = id;
        this.user = user;
        this.text = text;
        this.targetId = targetId;
        this.type = type;
        this.parentCommentUser = parentCommentUser;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
        this.isModified = isModified;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.reportCount = reportCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CommentUserResponse getUser() {
        return user;
    }

    public void setUser(CommentUserResponse user) {
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Target.TargetType getType() {
        return type;
    }

    public void setType(Target.TargetType type) {
        this.type = type;
    }

    public List<CommentResponse> getReplies() {
        return replies;
    }

    public void setReplies(List<CommentResponse> replies) {
        this.replies = replies;
    }

    public CommentUserResponse getParentCommentUser() {
        return parentCommentUser;
    }

    public void setParentCommentUser(CommentUserResponse parentCommentUser) {
        this.parentCommentUser = parentCommentUser;
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

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public Boolean getModified() {
        return isModified;
    }

    public void setModified(Boolean modified) {
        isModified = modified;
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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
