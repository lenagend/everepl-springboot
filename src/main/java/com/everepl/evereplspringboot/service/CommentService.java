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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
        Comment newComment = toEntity(commentRequest, userIp, passwordEncoder);

        // 대댓글인 경우 부모 댓글 설정
        if (commentRequest.type() == Comment.targetType.COMMENT) {
            Comment parentComment = commentRepository.findById(commentRequest.targetId())
                    .orElseThrow(() -> new NoSuchElementException("부모 댓글을 찾을 수 없습니다."));
            newComment.setParentComment(parentComment);
        }

        // 댓글 저장하여 ID를 생성 및 부모 댓글 설정 반영
        newComment = commentRepository.save(newComment);

        // 댓글 응답 생성 및 반환
        return toDto(newComment);
    }




    public Page<CommentResponse> getComments(CommentRequest commentRequest, Pageable pageable) {
        Page<CommentResponse> comments = commentRepository.findAllByTypeAndTargetId(
                commentRequest.type(), commentRequest.targetId(), pageable).map(CommentService::toDto);

        return comments;
    }

    public CommentResponse updateComment(CommentRequest commentRequest) {
        Long commentId = commentRequest.targetId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("해당 댓글을 찾을 수 없습니다."));

        // 비밀번호가 제공되었는지 확인하고, 제공된 경우 일치하는지 확인
        if (commentRequest.password() != null && !passwordEncoder.matches(commentRequest.password(), comment.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // Comment 객체 업데이트
        updateEntity(comment, commentRequest, passwordEncoder);

        comment.setUpdatedAt(LocalDateTime.now());
        Comment updatedComment = commentRepository.save(comment);

        return toDto(updatedComment);
    }



    public static CommentResponse toDto(Comment comment) {
        // 삭제된 댓글인 경우 대체 텍스트 설정
        String text = comment.isDeleted() ? "삭제된 댓글입니다" : comment.getText();

        return new CommentResponse(
                comment.getId(),
                comment.getUserIp(),
                comment.getNickname(),
                text, // 수정된 텍스트 사용
                comment.getTargetId(),
                comment.getType(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.isDeleted(),
                comment.getReplyCount(),
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

    public static void updateEntity(Comment comment, CommentRequest request, PasswordEncoder passwordEncoder) {
        if (request.nickname() != null) {
            comment.setNickname(request.nickname());
        }

        if (request.text() != null) {
            comment.setText(request.text());
        }

        if (request.password() != null) {
            String encryptedPassword = passwordEncoder.encode(request.password());
            comment.setPassword(encryptedPassword);
        }

        if (request.targetId() != null) {
            comment.setTargetId(request.targetId());
        }

        if (request.type() != null) {
            comment.setType(request.type());
        }

        Boolean isDeleted = request.isDeleted() != null ? request.isDeleted() : false;
        comment.setDeleted(isDeleted);
    }


}
