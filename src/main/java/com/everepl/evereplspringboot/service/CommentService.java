package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.CommentRequest;
import com.everepl.evereplspringboot.dto.CommentResponse;
import com.everepl.evereplspringboot.dto.CommentUserResponse;
import com.everepl.evereplspringboot.entity.Comment;
import com.everepl.evereplspringboot.entity.Target;
import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.repository.CommentRepository;
import com.everepl.evereplspringboot.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final UrlInfoService urlInfoService;
    private final UserService userService;
    private SimpMessagingTemplate messagingTemplate;
    private ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public CommentService(CommentRepository commentRepository, UrlInfoService urlInfoService, UserService userService, SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper, NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.urlInfoService = urlInfoService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    public CommentResponse addComment(CommentRequest commentRequest) {
        User currentUser = userService.getAuthenticatedUser();

        Comment newComment = toEntity(commentRequest, currentUser);

        // 먼저 댓글 저장하여 ID를 생성
        newComment = commentRepository.save(newComment);

        // 대댓글인 경우 부모 댓글 설정 및 path 계산
        if (commentRequest.type() == Target.TargetType.COMMENT) {
            Comment parentComment = findCommentById(commentRequest.targetId());

            newComment.setParentComment(parentComment);

            // 부모 댓글의 path와 현재 댓글의 ID를 사용하여 새로운 path 생성
            String newPath = parentComment.getPath() + "/" + newComment.getId();
            newComment.setPath(newPath);

            //최상위 댓글의 타겟타입 저장
            newComment.setRootTargetType(parentComment.getRootTargetType());

            // 부모 댓글의 commentCount를 업데이트
            parentComment.updateCommentCount(1);
            commentRepository.save(parentComment); // 변경된 부모 댓글을 저장

            if(parentComment.getUser().isNotificationSetting()){
                notifyUserAboutComment(parentComment, newComment);
            }

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


    @Async
    public void notifyUserAboutComment(Comment parentComment, Comment newComment) {
        String userTopic = "/topic/user." + parentComment.getUser().getId();
        try {
            Comment rootComment = findRootComment(newComment);
            String rootUrl = createRootUrl(rootComment);
            CommentResponse commentResponse = toDto(newComment, rootUrl);
            String jsonMessage = objectMapper.writeValueAsString(commentResponse);
            messagingTemplate.convertAndSend(
                    userTopic,
                    jsonMessage);

            String notificationTitle = StringUtils.truncateText(parentComment.getText(), 10) + "...글에 답글이 달렸습니다";
            notificationService.createNotificationForComment(commentResponse, notificationTitle);
        } catch (Exception e) {
           throw new MessagingException(e.getMessage());
        }
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
        if (commentRequest.type() == Target.TargetType.URLINFO) {
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

    // 댓글 업데이트 로직
    public CommentResponse updateComment(CommentRequest commentRequest) {
        Comment comment = findCommentById(commentRequest.targetId());

        User currentUser = userService.getAuthenticatedUser();

        validateCommentOwner(comment, currentUser.getId());

        comment.setText(commentRequest.text());

        comment.setModified(true);

        comment.setUpdatedAt(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);

        return toDto(updatedComment);
    }

    // 댓글 '삭제' 로직
    public CommentResponse deleteComment(Long id) {
        Comment comment = findCommentById(id);

        User currentUser = userService.getAuthenticatedUser();

        validateCommentOwner(comment, currentUser.getId());

        comment.setDeleted(true);

        comment.setUpdatedAt(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);

        return toDto(updatedComment);
    }

    // 댓글을 ID로 찾는 메소드
    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("해당 댓글을 찾을 수 없습니다."));
    }

    // 댓글의 주인을 검증하는 메소드
    private void validateCommentOwner(Comment comment, Long userId) {
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalStateException("댓글을 수정할 권한이 없습니다.");
        }
    }
    // 증감
    public void updateLikeCount(Long commentId, int increment) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("댓글 정보가 존재하지 않습니다: " + commentId));
        comment.updateLikeCount(increment); // 메서드명과 로직 변경
        updatePopularityScore(comment);
    }

    // 인기도 점수 계산 및 저장 (UrlInfo 객체가 이미 조회된 상태)
    @Async
    public void updatePopularityScore(Comment comment) {
        double score = calculatePopularityScore(comment);
        comment.setPopularityScore(score);
        commentRepository.save(comment);
    }

    private double calculatePopularityScore(Comment comment) {
        // 가중치 설정
        double likeWeight = 0.6;   // 좋아요에 대한 높은 가중치
        double commentWeight = 0.3; // 댓글에 대한 중간 가중치
        double reportPenalty = 0.4; // 신고에 대한 감점

        // 계산 로직
        return  (comment.getLikeCount() * likeWeight) +
                (comment.getCommentCount() * commentWeight) -
                (comment.getReportCount() * reportPenalty);
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

    public Page<CommentResponse> getMyComments(Pageable pageable) {
        User currentUser = userService.getAuthenticatedUser();

        return commentRepository.findByUser(currentUser, pageable)
                .map(comment -> {
                    Comment rootComment = findRootComment(comment);
                    String rootUrl = createRootUrl(rootComment);
                    return toDto(comment, rootUrl);
                });

    }


    public CommentResponse toDto(Comment comment) {
        return toDto(comment, null); // rootUrl 없이 메서드 호출
    }

    public CommentResponse toDto(Comment comment, String rootUrl) {
        String text = getCommentText(comment);

        // User 정보에서 UserDto 생성
        CommentUserResponse user = new CommentUserResponse(
                comment.getUser().getId(),
                comment.getUser().getDisplayName(),
                comment.getUser().getImageUrl()
        );

        // 부모 댓글의 User 정보를 Optional을 통해 안전하게 처리
        CommentUserResponse parentCommentUser = Optional.ofNullable(comment.getParentComment())
                .map(parentComment -> new CommentUserResponse(
                        parentComment.getUser().getId(),
                        parentComment.getUser().getDisplayName(),
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
                comment.isModified(),
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
