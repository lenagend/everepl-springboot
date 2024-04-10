package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.LikeRequest;
import com.everepl.evereplspringboot.dto.LikeResponse;
import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.entity.UserLike;
import com.everepl.evereplspringboot.entity.Target;
import com.everepl.evereplspringboot.exceptions.AlreadyExistsException;
import com.everepl.evereplspringboot.repository.UserLikeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserLikeService {

    private final CommentService commentService;
    private final UrlInfoService urlInfoService;
    private final UserLikeRepository userLikeRepository;
    private final UserInfoService userInfoService;

    public UserLikeService(CommentService commentService, UrlInfoService urlInfoService, UserLikeRepository userLikeRepository, UserInfoService userInfoService) {
        this.commentService = commentService;
        this.urlInfoService = urlInfoService;
        this.userLikeRepository = userLikeRepository;
        this.userInfoService = userInfoService;
    }

    public LikeResponse addLike(LikeRequest likeRequest) {
        LocalDate today = LocalDate.now();

        User currentUser = userInfoService.getAuthenticatedUser();

        // 오늘 해당 IP에서 해당 대상에 대해 좋아요를 이미 추가했는지 확인
        boolean alreadyLiked = userLikeRepository.existsByUserAndTargetTargetIdAndTargetTypeAndLikedDate(
                currentUser, likeRequest.targetId(), likeRequest.type(), today);

        if (alreadyLiked) {
            // 이미 좋아요를 추가했다면, 추가적인 처리 없이 종료
            throw new AlreadyExistsException("이미 좋아요 했습니다.");
        } else {
            // Like 엔티티 생성 및 저장
            UserLike userLike = toEntity(likeRequest, currentUser);
            userLikeRepository.save(userLike);

            // 좋아요 수 업데이트 로직 (예: URLInfo, Comment 등에 대한 처리)
            updateLikeCount(likeRequest, 1); // 좋아요 수를 1 증가시킴

            // LikeResponse 생성 및 반환
            return new LikeResponse(userLike.getId(), userLike.getTarget().getTargetId(), userLike.getTarget().getType());
        }
    }

    private UserLike toEntity(LikeRequest likeRequest, User user) {
        Target target = new Target(likeRequest.targetId(), likeRequest.type()); // Target 객체 생성
        UserLike userLike = new UserLike();
        userLike.setTarget(target); // Like 엔티티에 Target 설정
        userLike.setUser(user); // 사용자 IP 설정
        userLike.setLikedDate(LocalDate.now()); // 현재 날짜로 설정
        return userLike;
    }

    private void updateLikeCount(LikeRequest likeRequest, int increment) {
        switch (likeRequest.type()) {
            case URLINFO:
                urlInfoService.updateLikeCount(likeRequest.targetId(), increment);
                break;
            case COMMENT:
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 타입입니다: " + likeRequest.type());
        }
    }

    public Page<?> processUserLikes(LikeRequest likeRequest, Pageable pageable) {
        User currentUser = userInfoService.getAuthenticatedUser(); // 현재 로그인한 사용자 정보를 가져옴
        Target.TargetType targetType = likeRequest.type();
        List<Long> targetIds = userLikeRepository.findTargetIdsByUserAndTargetType(currentUser, targetType);

        Page<?> pageResponse;
        switch (targetType) {
            case URLINFO:
                // UrlInfoService에서 주어진 ID 리스트에 해당하는 UrlInfoResponse 객체들을 페이징 처리하여 조회
                pageResponse = urlInfoService.getUrlInfoByIds(targetIds, pageable);
                break;
            case COMMENT:
                pageResponse = commentService.getCommentsByIdsWithRootUrl(targetIds, pageable);
                break;
            // 기타 타입에 대한 처리...
            default:
                throw new IllegalArgumentException("Unsupported target type: " + targetType);
        }

        return pageResponse;
    }

    public LikeResponse toDto(UserLike userLike) {
        return new LikeResponse(
                userLike.getId(), // Like ID
                userLike.getTarget().getTargetId(), // Target ID
                userLike.getTarget().getType() // Target 타입
        );
    }


}
