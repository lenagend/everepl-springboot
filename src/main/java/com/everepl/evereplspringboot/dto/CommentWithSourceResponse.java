package com.everepl.evereplspringboot.dto;

public class CommentWithSourceResponse {
    private CommentResponse comment;
    private String sourceTitle;
    private String sourceLink;

    public CommentWithSourceResponse(CommentResponse comment, String sourceTitle, String sourceLink) {
        this.comment = comment;
        this.sourceTitle = sourceTitle;
        this.sourceLink = sourceLink;
    }

    public CommentResponse getComment() {
        return comment;
    }

    public void setComment(CommentResponse comment) {
        this.comment = comment;
    }

    public String getSourceTitle() {
        return sourceTitle;
    }

    public void setSourceTitle(String sourceTitle) {
        this.sourceTitle = sourceTitle;
    }

    public String getSourceLink() {
        return sourceLink;
    }

    public void setSourceLink(String sourceLink) {
        this.sourceLink = sourceLink;
    }
}