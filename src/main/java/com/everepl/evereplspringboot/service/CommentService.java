package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.CommentRequest;
import com.everepl.evereplspringboot.dto.CommentResponse;
import com.everepl.evereplspringboot.entity.Comment;
import com.everepl.evereplspringboot.repository.CommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    private static final Logger log = LoggerFactory.getLogger(UrlInfoService.class);
    private final CommentRepository commentRepository;
    private final UrlInfoService urlInfoService;

    public CommentService(CommentRepository commentRepository, UrlInfoService urlInfoService) {
        this.commentRepository = commentRepository;
        this.urlInfoService = urlInfoService;
    }

    public CommentResponse addComment(CommentRequest commentRequest) {
        // 댓글 저장 로직
        CommentResponse savedComment = toDto(commentRepository.save(toEntity(commentRequest)));

        // UrlInfo의 댓글 수 업데이트 및 인기도 점수 업데이트
        if (commentRequest.type() == Comment.targetType.URLINFO) {
            urlInfoService.updatePopularityScore(commentRequest.targetId());
        }

        return savedComment;
    }

    public static CommentResponse toDto(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getNickname(),
                comment.getText(),
                comment.getTargetId(),
                comment.getType(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getCommentCount(),
                comment.getLikeCount(),
                comment.getReportCount(),
                comment.getPopularityScore()
        );
    }

    public static Comment toEntity(CommentRequest request) {
        Comment comment = new Comment();
        comment.setNickname(request.nickname());
        comment.setText(request.text());
        comment.setPassword(request.password()); // 여기서 비밀번호를 다루는 방법을 고려해야 함
        comment.setTargetId(request.targetId());
        comment.setType(request.type());
        return comment;
    }
}
