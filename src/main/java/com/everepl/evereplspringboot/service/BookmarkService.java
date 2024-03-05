package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.BookmarkRequest;
import com.everepl.evereplspringboot.entity.Target;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class BookmarkService {
    private final UrlInfoService urlInfoService;
    private final CommentService commentService;

    // 서비스의 생성자에 @Autowired 어노테이션을 사용하여 의존성 주입
    @Autowired
    public BookmarkService(UrlInfoService urlInfoService, CommentService commentService) {
        this.urlInfoService = urlInfoService;
        this.commentService = commentService;
    }

    public Page<?> processBookmarks(BookmarkRequest bookmarkRequest, Pageable pageable) {
        Page<?> pageResponse;
        Target.TargetType targetType = bookmarkRequest.targetType();
        List<Long> targetIds = bookmarkRequest.targetIds();

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


}
