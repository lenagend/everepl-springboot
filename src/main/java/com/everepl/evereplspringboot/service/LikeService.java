package com.everepl.evereplspringboot.service;

import org.springframework.stereotype.Service;

@Service
public class LikeService {

    private final CommentService commentService;
    private final UrlInfoService urlInfoService;

    // 생성자를 통한 의존성 주입
    public LikeService(CommentService commentService, UrlInfoService urlInfoService) {
        this.commentService = commentService;
        this.urlInfoService = urlInfoService;
    }


    public void addLikeToUrlInfo(Long urlInfoId) {
        //urlInfoService.incrementLikeCount(urlInfoId);
    }

    // 좋아요 제거 메소드도 유사하게 구현
}
