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

        // 먼저 댓글 저장하여 ID를 생성
        newComment = commentRepository.save(newComment);

        // 대댓글인 경우 부모 댓글 설정 및 path 계산
        if (commentRequest.type() == Comment.targetType.COMMENT) {
            Comment parentComment = commentRepository.findById(commentRequest.targetId())
                    .orElseThrow(() -> new NoSuchElementException("부모 댓글을 찾을 수 없습니다."));
            newComment.setParentComment(parentComment);

            // 부모 댓글의 path와 현재 댓글의 ID를 사용하여 새로운 path 생성
            String newPath = parentComment.getPath() + "/" + newComment.getId();
            newComment.setPath(newPath);
        } else {
            // 루트 댓글인 경우, path는 댓글의 ID
            newComment.setPath(commentRequest.targetId() + "/" + newComment.getId());
        }

        // path가 업데이트된 댓글을 다시 저장
        newComment = commentRepository.save(newComment);

        // 댓글 응답 생성 및 반환
        CommentResponse savedComment = toDto(newComment);


        //루트댓글의 타켓타입과 타겟ID로 해당 엔티티의 commentCount 업데이트
        Comment rootComment = findRootComment(newComment);

        if (rootComment.getType() == Comment.targetType.URLINFO) {
            urlInfoService.updateCommentCount(rootComment.getTargetId(), 1);
            urlInfoService.updatePopularityScore(rootComment.getTargetId());
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

        int commentCount = 0;
        if (commentRequest.type() == Comment.targetType.URLINFO) {
            commentCount = urlInfoService.getCommentCountForUrlInfo(commentRequest.targetId());
        }

        // PageImpl를 사용하여 페이징된 결과를 반환합니다.
        return new PageImpl<>(commentResponses, pageable, commentCount);
    }

    private Comment findRootComment(Comment comment) {
        while (comment.getParentComment() != null) {
            comment = comment.getParentComment();
        }
        return comment;
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
        return new CommentResponse(
                comment.getId(),
                comment.getUserIp(),
                comment.getNickname(),
                comment.getText(),
                comment.getTargetId(),
                comment.getType(),
                comment.getPath(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.isDeleted(),
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
