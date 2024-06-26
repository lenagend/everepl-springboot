package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.*;
import com.everepl.evereplspringboot.entity.Comment;
import com.everepl.evereplspringboot.entity.Target;
import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.repository.CommentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
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

    public CommentService(CommentRepository commentRepository, UrlInfoService urlInfoService, @Lazy UserService userService, SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper, NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.urlInfoService = urlInfoService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    public CommentResponse addComment(CommentRequest commentRequest) {
        User currentUser = userService.getAuthenticatedUser();

        if (currentUser.getCommentBanUntil() != null && currentUser.getCommentBanUntil().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("신고누적으로 다음 기간까지 댓글을 작성하실 수 없습니다. " + currentUser.getCommentBanUntil());
        }

        Comment newComment = toEntity(commentRequest, currentUser);

        // 댓글 저장하여 ID 생성
        newComment = commentRepository.save(newComment);

        if (commentRequest.type() == Target.TargetType.COMMENT) {
            Comment parentComment = findCommentById(commentRequest.targetId());
            newComment.setParentComment(parentComment);

            // 부모 댓글의 commentCount 업데이트
            parentComment.updateCommentCount(1);
            commentRepository.save(parentComment);

            // 부모 댓글 사용자에게 알림
            if (!parentComment.getUser().equals(newComment.getUser())) {
                if (parentComment.getUser().isNotificationSetting()) {
                    notifyUserAboutComment(parentComment, newComment);
                }
            }
        }

        // 댓글 응답 생성 및 반환
        CommentResponse savedComment = toDto(newComment);

        // 루트 댓글의 타겟 타입에 따라 연관된 엔티티의 commentCount 및 인기도 점수 업데이트
        Comment rootComment = findRootComment(newComment);
        if (rootComment.getTarget().getType() == Target.TargetType.URLINFO) {
            urlInfoService.updateCommentCount(rootComment.getTarget().getTargetId(), 1);
            urlInfoService.updatePopularityScore(rootComment.getTarget().getTargetId());
        }

        return savedComment;
    }

    @Async
    public void notifyUserAboutComment(Comment parentComment, Comment newComment) {
        String userTopic = "/topic/user." + parentComment.getUser().getId();
        try {
            Comment rootComment = findRootComment(newComment);

            // 원본 newComment의 텍스트를 가져와 변환
            String htmlContent = newComment.getText();
            String processedText = extractTextWithEmojiReplacement(htmlContent);

            // 새로운 CommentResponse 객체 생성
            CommentResponse commentResponse = toDto(newComment);
            commentResponse.setText(processedText);  // 변환된 텍스트로 설정

            String link = createSourceLink(rootComment);
            commentResponse.setLink(link);

            NotificationResponse notificationResponse = notificationService.createNotificationForComment(commentResponse);

            String jsonMessage = objectMapper.writeValueAsString(notificationResponse);
            messagingTemplate.convertAndSend(
                    userTopic,
                    jsonMessage);
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }

    private String extractTextWithEmojiReplacement(String html) {
        Document document = Jsoup.parse(html);
        Elements images = document.select("img");
        for (Element img : images) {
            if (img.attr("alt").equals("이모티콘")) {
                img.replaceWith(new Element(Tag.valueOf("span"), "").text("[이모티콘]"));
            }
        }
        return document.text();
    }


    public List<CommentResponse> getComments(CommentRequest commentRequest) {
        // 주 댓글만 가져오기
        List<Comment> comments = commentRepository.findByTarget_TypeAndTarget_TargetId(
                commentRequest.type(), commentRequest.targetId());

        // 결과를 CommentResponse DTO로 변환하면서 초기 대댓글 로드
        List<CommentResponse> commentResponses = comments.stream()
                .map(comment -> {
                    CommentResponse response = toDto(comment);  // 댓글을 DTO로 변환
                    List<Comment> initialReplies = commentRepository.findTop3ByParentCommentIdOrderByCreatedAtAsc(comment.getId());
                    List<CommentResponse> replyResponses = initialReplies.stream().map(this::toDto).collect(Collectors.toList());
                    response.setReplies(replyResponses);  // 초기 대댓글 설정
                    return response;
                })
                .collect(Collectors.toList());

        return commentResponses;
    }


    public List<CommentResponse> getReplies(Long parentCommentId) {
        return commentRepository.findByParentCommentId(parentCommentId).stream().map(this::toDto).collect(Collectors.toList());
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

    public List<Comment> getCommentsByUser(User user) {
        return commentRepository.findByUser(user);
    }

    public void deleteAllComments(List<Comment> comments){
        User anonymousUser = userService.findOrCreateAnonymousUser();
        for (Comment comment : comments) {
            comment.setDeleted(true);
            comment.setUser(anonymousUser);
        }
        commentRepository.saveAll(comments);
    }

    public Page<CommentWithSourceResponse> getMyCommentsWithSources(Pageable pageable) {
        User currentUser = userService.getAuthenticatedUser();

        return commentRepository.findByUser(currentUser, pageable)
                .map(this::toDtoWithSource);
    }

    public Page<CommentWithSourceResponse> getCommentsByIdsWithSource(List<Long> ids, Pageable pageable) {
        Specification<Comment> spec = (root, query, criteriaBuilder) -> root.get("id").in(ids);

        Page<Comment> comments = commentRepository.findAll(spec, pageable);

        return comments.map(this::toDtoWithSource);
    }


    private String createSourceLink(Comment rootComment) {
        Target target = rootComment.getTarget();
        Target.TargetType targetType = target.getType();
        Long targetId = target.getTargetId();
        String url = "";
        switch (targetType) {
            case URLINFO:
                url = "/view/" + targetId + "?commentId=" + rootComment.getId();
                break;
            default:
                url = "/";
        }
        return url;
    }

    private String createSourceTitle(Comment rootComment) {
        Target target = rootComment.getTarget();
        Target.TargetType targetType = target.getType();
        Long targetId = target.getTargetId();
        String title = "";
        switch (targetType) {
            case URLINFO:
                title = urlInfoService.getUrlInfoById(targetId).getTitle();
                break;
            default:
                title = "";
        }
        return title;
    }

    public CommentWithSourceResponse toDtoWithSource(Comment comment) {
        CommentResponse commentResponse = toDto(comment);
        Comment rootComment = findRootComment(comment);
        String sourceTitle = createSourceTitle(rootComment);  // Target에 제목 필드가 있다고 가정
        String sourceLink = createSourceLink(rootComment);

        return new CommentWithSourceResponse(commentResponse, sourceTitle, sourceLink);
    }


    public CommentResponse toDto(Comment comment) {
        String text = getCommentText(comment);

        CommentUserResponse user = new CommentUserResponse(
                comment.getUser().getId(),
                comment.getUser().getDisplayName(),
                comment.getUser().getImageUrl()
        );

        CommentUserResponse parentCommentUser = Optional.ofNullable(comment.getParentComment())
                .map(parentComment -> new CommentUserResponse(
                        parentComment.getUser().getId(),
                        parentComment.getUser().getDisplayName(),
                        parentComment.getUser().getImageUrl()
                )).orElse(null);

        return new CommentResponse(
                comment.getId(),
                user,
                text,
                comment.getTarget().getTargetId(),
                comment.getTarget().getType(),
                parentCommentUser,
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.isDeleted(),
                comment.isModified(),
                comment.getCommentCount(),
                comment.getLikeCount(),
                comment.getReportCount()
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
