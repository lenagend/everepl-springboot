package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.CommentRequest;
import com.everepl.evereplspringboot.dto.CommentResponse;
import com.everepl.evereplspringboot.dto.UrlInfoResponse;
import com.everepl.evereplspringboot.dto.UserDto;
import com.everepl.evereplspringboot.entity.Comment;
import com.everepl.evereplspringboot.entity.Target;
import com.everepl.evereplspringboot.entity.UrlInfo;
import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.repository.CommentRepository;
import com.everepl.evereplspringboot.repository.UserRepository;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final UrlInfoService urlInfoService;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, UrlInfoService urlInfoService, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.urlInfoService = urlInfoService;
        this.userRepository = userRepository;
    }

    public CommentResponse addComment(CommentRequest commentRequest) {
        // commentRequest에서 userId를 추출
        Long userId = commentRequest.userId();

        // UserRepository를 사용하여 userId에 해당하는 User 객체를 찾음
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(""));

        Comment newComment = toEntity(commentRequest, user);

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
        // 댓글 ID로 댓글 조회
        Comment comment = commentRepository.findById(commentRequest.targetId())
                .orElseThrow(() -> new NoSuchElementException("해당 댓글을 찾을 수 없습니다."));

        // 댓글 삭제 요청이 아닌 경우에만 텍스트 업데이트
        if (Boolean.FALSE.equals(commentRequest.isDeleted())) {
            comment.setText(commentRequest.text());
        }

        // 댓글 삭제 상태 설정
        comment.setDeleted(Boolean.TRUE.equals(commentRequest.isDeleted()));

        // 업데이트된 시간 설정
        comment.setUpdatedAt(LocalDateTime.now());

        // 변경사항 저장 및 반환
        Comment updatedComment = commentRepository.save(comment);
        return toDto(updatedComment);
    }


    public Page<CommentResponse> getCommentsByIdsWithRootUrl(List<Long> ids, Pageable pageable) {
        Specification<Comment> spec = (root, query, criteriaBuilder) -> root.get("id").in(ids);

        Page<Comment> comments = commentRepository.findAll(spec, pageable);

        return comments.map(comment -> {
            Comment rootComment = findRootComment(comment);
            String rootUrl = createRootUrl(rootComment);
            return toDto(comment, rootUrl);
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
        return toDto(comment, null); // rootUrl 없이 메서드 호출
    }

    public CommentResponse toDto(Comment comment, String rootUrl) {
        String text = getCommentText(comment);

        // User 정보에서 UserDto 생성
        UserDto user = new UserDto(
                comment.getUser().getId(),
                comment.getUser().getName(),
                comment.getUser().getImageUrl()
        );

        // 부모 댓글의 User 정보를 Optional을 통해 안전하게 처리
        UserDto parentCommentUser = Optional.ofNullable(comment.getParentComment())
                .map(parentComment -> new UserDto(
                        parentComment.getUser().getId(),
                        parentComment.getUser().getName(),
                        parentComment.getUser().getImageUrl()
                )).orElse(null); // 부모 댓글이 없는 경우 null을 반환


        return new CommentResponse(
                comment.getId(),
                user,
                text,
                comment.getTarget().getTargetId(),
                comment.getTarget().getType(),
                parentCommentUser,
                comment.getPath(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.isDeleted(),
                comment.getCommentCount(),
                comment.getLikeCount(),
                comment.getReportCount(),
                rootUrl // rootUrl 전달 (null일 수 있음)
        );
    }


    private String getCommentText(Comment comment) {
        return comment.isDeleted() ? "삭제된 댓글입니다" : comment.getText();
    }

    public static Comment toEntity(CommentRequest request, User user) {
        Comment comment = new Comment();
        comment.setText(request.text());
        comment.setUser(user); // Comment 객체에 User 객체 설정

        Target target = new Target(); // Target 객체 생성
        target.setTargetId(request.targetId()); // targetId 설정
        target.setType(request.type()); // type 설정
        comment.setTarget(target); // Comment 객체에 Target 객체 설정

        return comment;
    }






}
