package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.BookmarkRequest;
import com.everepl.evereplspringboot.dto.BookmarkResponse;
import com.everepl.evereplspringboot.dto.UrlInfoResponse;
import com.everepl.evereplspringboot.entity.Comment;
import com.everepl.evereplspringboot.entity.Target;
import com.everepl.evereplspringboot.entity.UrlInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<?> processBookmarks(BookmarkRequest bookmarkRequest, Pageable pageable) {
        List<?> responses;
        Target.TargetType targetType = bookmarkRequest.targetType();
        List<Long> targetIds = bookmarkRequest.targetIds();

        switch (targetType) {
            case URLINFO:
                // UrlInfoService에서 주어진 ID 리스트에 해당하는 UrlInfoResponse 객체들을 페이징 처리하여 조회
                Page<UrlInfoResponse> urlInfoResponses = urlInfoService.findByIds(targetIds, pageable);
                responses = urlInfoResponses.getContent(); // Page 객체에서 컨텐츠 추출
                break;
            case COMMENT:
                // COMMENT 처리 로직 (추후 구현)
                responses = Collections.emptyList(); // 임시 처리
                break;
            // 기타 타입에 대한 처리...
            default:
                throw new IllegalArgumentException("Unsupported target type: " + targetType);
        }

        return responses;
    }


}
