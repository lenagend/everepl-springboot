package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.CommentRequest;
import com.everepl.evereplspringboot.dto.CommentResponse;
import com.everepl.evereplspringboot.dto.UrlInfoResponse;
import com.everepl.evereplspringboot.entity.Comment;
import com.everepl.evereplspringboot.entity.Target;
import com.everepl.evereplspringboot.entity.UrlInfo;
import com.everepl.evereplspringboot.repository.CommentRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
        if (commentRequest.type() == Target.TargetType.COMMENT) {
            Comment parentComment = commentRepository.findById(commentRequest.targetId())
                    .orElseThrow(() -> new NoSuchElementException("부모 댓글을 찾을 수 없습니다."));
            newComment.setParentComment(parentComment);

            // 부모 댓글의 path와 현재 댓글의 ID를 사용하여 새로운 path 생성
            String newPath = parentComment.getPath() + "/" + newComment.getId();
            newComment.setPath(newPath);

            //최상위 댓글의 타겟타입 저장
            newComment.setRootTargetType(parentComment.getRootTargetType());

            // 부모 댓글의 commentCount를 업데이트
            parentComment.updateCommentCount(1);
            commentRepository.save(parentComment); // 변경된 부모 댓글을 저장
        } else {
            // 루트 댓글인 경우, path는 댓글의 ID
            newComment.setPath(newComment.getTarget().getTargetId() + "/" + newComment.getId()); // 수정됨

            //최상위 댓글의 타겟타입 저장
            newComment.setRootTargetType(commentRequest.type());
        }

        // path가 업데이트된 댓글을 다시 저장
        newComment = commentRepository.save(newComment);

        // 댓글 응답 생성 및 반환
        CommentResponse savedComment = toDto(newComment);

        // 루트댓글의 타켓타입과 타겟ID로 해당 엔티티의 commentCount 업데이트
        Comment rootComment = findRootComment(newComment);

        if (rootComment.getTarget().getType() == Target.TargetType.URLINFO) { // 수정됨
            urlInfoService.updateCommentCount(rootComment.getTarget().getTargetId(), 1); // 수정됨
            urlInfoService.updatePopularityScore(rootComment.getTarget().getTargetId()); // 수정됨
        }

        return savedComment;
    }



    public Page<CommentResponse> getComments(CommentRequest commentRequest, Pageable pageable) {
        // 새로운 커스텀 메서드를 호출합니다.
        List<Comment> comments = commentRepository.findCommentsWithRepliesByTarget_TypeAndTarget_TargetId(
                commentRequest.type(), commentRequest.targetId(), pageable);

        // 결과를 CommentResponse DTO로 변환합니다.
        List<CommentResponse> commentResponses = comments.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        int commentCount = 0;
        if (commentRequest.type() == Target.TargetType.COMMENT) {
            commentCount = urlInfoService.getCommentCountForUrlInfo(commentRequest.targetId());
        }

        // PageImpl를 사용하여 페이징된 결과를 반환합니다.
        return new PageImpl<>(commentResponses, pageable, commentCount);
    }

    public Page<CommentResponse> getCommentsByIds(List<Long> ids, Pageable pageable) {
        Specification<Comment> spec = new Specification<Comment>() {
            @Override
            public Predicate toPredicate(Root<Comment> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return root.get("id").in(ids);
            }
        };

        return commentRepository.findAll(spec, pageable).map(this::toDto);
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

    public Page<CommentResponse> getCommentsByIdsWithRootUrl(List<Long> ids, Pageable pageable) {
        Specification<Comment> spec = (root, query, criteriaBuilder) -> root.get("id").in(ids);

        Page<Comment> comments = commentRepository.findAll(spec, pageable);

        return comments.map(comment -> {
            Comment rootComment = findRootComment(comment);
            String rootUrl = createRootUrl(rootComment);
            return toDtoWithRootUrl(comment, rootUrl);
        });
    }

    private String createRootUrl(Comment rootComment) {
        Target target = rootComment.getTarget();
        Target.TargetType targetType = target.getType();
        Long targetId = target.getTargetId();
        String url = "";
        switch (targetType) {
            case URLINFO:
                    url = "/view/" + targetId;
                break;
            default:
               url = "/";
        }
        return url;
    }


    public CommentResponse toDto(Comment comment) {
        String text = getCommentText(comment);

        Pair<String, String> parentDetails = getParentCommentDetails(comment); // 추출된 메서드 사용
        String parentCommentNickname = parentDetails.getLeft(); // 'getFirst()' 대신 'getLeft()' 사용
        String parentCommentUserIp = parentDetails.getRight(); // 'getSecond()' 대신 'getRight()' 사용


        return new CommentResponse(
                comment.getId(),
                comment.getUserIp(),
                comment.getNickname(),
                text,
                comment.getTarget().getTargetId(), // 수정됨
                comment.getTarget().getType(), // 수정됨
                parentCommentNickname,
                parentCommentUserIp,
                comment.getPath(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.isDeleted(),
                comment.getCommentCount(),
                comment.getLikeCount(),
                comment.getReportCount(),
                null
        );
    }

    public CommentResponse toDtoWithRootUrl(Comment comment, String rootUrl) {
        String text = getCommentText(comment);

        Pair<String, String> parentDetails = getParentCommentDetails(comment); // 추출된 메서드 사용
        String parentCommentNickname = parentDetails.getLeft(); // 'getFirst()' 대신 'getLeft()' 사용
        String parentCommentUserIp = parentDetails.getRight(); // 'getSecond()' 대신 'getRight()' 사용


        return new CommentResponse(
                comment.getId(),
                comment.getUserIp(),
                comment.getNickname(),
                text,
                comment.getTarget().getTargetId(), // 수정됨
                comment.getTarget().getType(), // 수정됨
                parentCommentNickname,
                parentCommentUserIp,
                comment.getPath(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.isDeleted(),
                comment.getCommentCount(),
                comment.getLikeCount(),
                comment.getReportCount(),
                rootUrl
        );
    }

    private String getCommentText(Comment comment) {
        return comment.isDeleted() ? "삭제된 댓글입니다" : comment.getText();
    }

    private Pair<String, String> getParentCommentDetails(Comment comment) {
        String parentCommentNickname = null;
        String parentCommentUserIp = null;
        if (comment.getParentComment() != null) {
            parentCommentNickname = comment.getParentComment().getNickname();
            parentCommentUserIp = comment.getParentComment().getUserIp();
        }
        return Pair.of(parentCommentNickname, parentCommentUserIp);
    }


    public static Comment toEntity(CommentRequest request, String userIp, PasswordEncoder passwordEncoder) {
        Comment comment = new Comment();
        comment.setUserIp(userIp);
        comment.setNickname(request.nickname());
        comment.setText(request.text());

        String encryptedPassword = passwordEncoder.encode(request.password());
        comment.setPassword(encryptedPassword);

        Target target = new Target(); // Target 객체 생성
        target.setTargetId(request.targetId()); // targetId 설정
        target.setType(request.type()); // type 설정
        comment.setTarget(target); // Comment 객체에 Target 객체 설정

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

        // Target 객체를 업데이트합니다.
        Target target = comment.getTarget(); // 기존 Target 객체를 가져옵니다.
        if (target == null) {
            target = new Target(); // Target 객체가 없으면 새로 생성합니다.
            comment.setTarget(target); // 새로운 Target 객체를 Comment에 설정합니다.
        }

        if (request.targetId() != null) {
            target.setTargetId(request.targetId()); // Target 객체의 targetId를 업데이트합니다.
        }

        if (request.type() != null) {
            target.setType(request.type()); // Target 객체의 type을 업데이트합니다.
        }

        Boolean isDeleted = request.isDeleted() != null ? request.isDeleted() : false;
        comment.setDeleted(isDeleted);
    }



}
