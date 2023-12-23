package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.UrlInfoResponse;
import com.everepl.evereplspringboot.entity.UrlInfo;
import com.everepl.evereplspringboot.repository.UrlInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UrlInfoService {
    private final UrlInfoRepository urlInfoRepository;
    @Autowired
    public UrlInfoService(UrlInfoRepository urlInfoRepository) {
        this.urlInfoRepository = urlInfoRepository;
    }



    public UrlInfoResponse processUrl(String url) {
        // 데이터베이스에서 URL 조회
        Optional<UrlInfo> existingUrlInfo = urlInfoRepository.findByUrl(url);

        if (existingUrlInfo.isPresent()) {
            // URL 정보가 이미 존재하면 반환
            return convertToDto(existingUrlInfo.get());
        } else {
            // URL 정보가 없으면 새로 수집, 저장 후 반환
            UrlInfo urlInfo = new UrlInfo();
            // URL로부터 필요한 정보 수집 (예: 제목, 파비콘 등)
            // ...
            urlInfo.setUrl(url);
            // urlInfo에 다른 정보 설정
            // ...
            urlInfoRepository.save(urlInfo);
            return convertToDto(urlInfo);
        }
    }

    public UrlInfoResponse convertToDto(UrlInfo urlInfo) {
        return new UrlInfoResponse(
                urlInfo.getId(),
                urlInfo.getUrl(),
                urlInfo.getTitle(),
                urlInfo.getFaviconSrc(),
                urlInfo.getCreatedAt(),
                urlInfo.getUpdatedAt(),
                urlInfo.getViewCount(),
                urlInfo.getCommentCount(),
                urlInfo.getReportCount(),
                urlInfo.getLastComment()
        );
    }

}
