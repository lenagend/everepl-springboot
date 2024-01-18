package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.CommentRequest;
import com.everepl.evereplspringboot.dto.CommentResponse;
import com.everepl.evereplspringboot.entity.Comment;
import com.everepl.evereplspringboot.repository.CommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private static final Logger log = LoggerFactory.getLogger(UrlInfoService.class);
    private final CommentRepository commentRepository;
    private final UrlInfoService urlInfoService;
    private final PasswordEncoder passwordEncoder;

    public CommentService(CommentRepository commentRepository, UrlInfoService urlInfoService, PasswordEncoder passwordEncoder) {
        this.commentRepository = commentRepository;
        this.urlInfoService = urlInfoService;
        this.passwordEncoder = passwordEncoder;
    }

    public CommentResponse addComment(CommentRequest commentRequest, String userIp) {
        // 댓글 저장 로직
        CommentResponse savedComment = toDto(commentRepository.save(toEntity(commentRequest, userIp, passwordEncoder)));

        // UrlInfo의 댓글 수 업데이트 및 인기도 점수 업데이트
        if (commentRequest.type() == Comment.targetType.URLINFO) {
            urlInfoService.updatePopularityScore(commentRequest.targetId());
        }

        return savedComment;
    }

    public Page<CommentResponse> getComments(CommentRequest commentRequest, Pageable pageable) {
        // 새로운 커스텀 메서드를 호출합니다.
        List<Comment> comments = commentRepository.findCommentsWithRepliesByTargetTypeAndTargetId(
                commentRequest.type(), commentRequest.targetId(), pageable);

        // 결과를 CommentResponse DTO로 변환합니다.
        List<CommentResponse> commentResponses = comments.stream()
                .map(CommentService::toDto)
                .collect(Collectors.toList());

        // 총 댓글 수를 가져옵니다. 이는 페이징을 위해 필요합니다.
        // 이 부분은 프로젝트의 구체적인 요구사항에 따라 다를 수 있으며, 필요에 따라 수정합니다.
        long total = commentRepository.countTotalCommentsIncludingRepliesByTargetTypeAndTargetId(
                commentRequest.type(), commentRequest.targetId());

        // PageImpl를 사용하여 페이징된 결과를 반환합니다.
        return new PageImpl<>(commentResponses, pageable, total);
    }


    public static CommentResponse toDto(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getUserIp(),
                comment.getNickname(),
                comment.getText(),
                comment.getTargetId(),
                comment.getType(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getCommentCount(),
                comment.getLikeCount(),
                comment.getReportCount()
        );
    }

    public static Comment toEntity(CommentRequest request, String userIp, PasswordEncoder passwordEncoder) {
        Comment comment = new Comment();
        comment.setUserIp(userIp);
        comment.setNickname(request.nickname());
        comment.setText(request.text());

        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(request.password());
        comment.setPassword(encryptedPassword);

        comment.setTargetId(request.targetId());
        comment.setType(request.type());
        return comment;
    }

}
