package com.everepl.evereplspringboot.service;

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

    public Comment addComment(Comment comment) {
        // 댓글 저장 로직
        Comment savedComment = commentRepository.save(comment);

        // UrlInfo의 댓글 수 업데이트 및 인기도 점수 업데이트
        if (comment.getType() == Comment.targetType.URLINFO) {
            urlInfoService.updatePopularityScore(comment.getTargetId());
        }

        return savedComment;
    }

}
