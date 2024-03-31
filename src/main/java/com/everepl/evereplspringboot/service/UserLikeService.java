package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.LikeRequest;
import com.everepl.evereplspringboot.dto.LikeResponse;
import com.everepl.evereplspringboot.entity.UserLike;
import com.everepl.evereplspringboot.entity.Target;
import com.everepl.evereplspringboot.exceptions.AlreadyExistsException;
import com.everepl.evereplspringboot.repository.UserLikeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserLikeService {

    private final CommentService commentService;
    private final UrlInfoService urlInfoService;
    private final UserLikeRepository userLikeRepository;

    // 생성자를 통한 의존성 주입
    public UserLikeService(CommentService commentService, UrlInfoService urlInfoService, UserLikeRepository userLikeRepository) {
        this.commentService = commentService;
        this.urlInfoService = urlInfoService;
        this.userLikeRepository = userLikeRepository;
    }

    public LikeResponse addLike(LikeRequest likeRequest, String userIp) {
        LocalDate today = LocalDate.now();
        // 오늘 해당 IP에서 해당 대상에 대해 좋아요를 이미 추가했는지 확인
        boolean alreadyLiked = userLikeRepository.existsByUserIpAndTargetTargetIdAndTargetTypeAndLikedDate(
                userIp, likeRequest.targetId(), likeRequest.type(), today);

        if (alreadyLiked) {
            // 이미 좋아요를 추가했다면, 추가적인 처리 없이 종료
            throw new AlreadyExistsException("이미 좋아요 했습니다.");
        } else {
            // Like 엔티티 생성 및 저장
            UserLike userLike = toEntity(likeRequest, userIp);
            userLikeRepository.save(userLike);

            // 좋아요 수 업데이트 로직 (예: URLInfo, Comment 등에 대한 처리)
            updateLikeCount(likeRequest, 1); // 좋아요 수를 1 증가시킴

            // LikeResponse 생성 및 반환
            return new LikeResponse(userLike.getId(), userLike.getTarget().getTargetId(), userLike.getTarget().getType());
        }
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



    private LikeResponse saveLike(Long targetId, Target.TargetType type, String userIp) {
        UserLike userLike = new UserLike();
        Target target = new Target(); // Target 객체 생성
        target.setTargetId(targetId); // Target ID 설정
        target.setType(type); // Target 타입 설정

        userLike.setUserIp(userIp); // 사용자 IP 설정
        userLike.setTarget(target); // Like 엔티티에 Target 설정
        userLike.setLikedDate(LocalDate.now()); // 현재 날짜로 설정

        UserLike savedUserLike = userLikeRepository.save(userLike); // Like 엔티티 저장

        // 저장된 Like 엔티티를 바탕으로 LikeResponse 생성 및 반환
        return new LikeResponse(savedUserLike.getId(), savedUserLike.getTarget().getTargetId(), savedUserLike.getTarget().getType());
    }


    private UserLike toEntity(LikeRequest likeRequest, String userIp) {
        Target target = new Target(likeRequest.targetId(), likeRequest.type()); // Target 객체 생성
        UserLike userLike = new UserLike();
        userLike.setTarget(target); // Like 엔티티에 Target 설정
        userLike.setUserIp(userIp); // 사용자 IP 설정
        userLike.setLikedDate(LocalDate.now()); // 현재 날짜로 설정
        return userLike;
    }



    public LikeResponse toDto(UserLike userLike) {
        return new LikeResponse(
                userLike.getId(), // Like ID
                userLike.getTarget().getTargetId(), // Target ID
                userLike.getTarget().getType() // Target 타입
        );
    }


}
