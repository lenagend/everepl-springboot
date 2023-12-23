package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.UrlInfoResponse;
import com.everepl.evereplspringboot.eceptions.InvalidUrlException;
import com.everepl.evereplspringboot.entity.UrlInfo;
import com.everepl.evereplspringboot.repository.UrlInfoRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;
import java.util.Optional;

@Service
public class UrlInfoService {
    private static final Logger log = LoggerFactory.getLogger(UrlInfoService.class);
    private final UrlInfoRepository urlInfoRepository;
    @Autowired
    public UrlInfoService(UrlInfoRepository urlInfoRepository) {
        this.urlInfoRepository = urlInfoRepository;
    }


    public UrlInfoResponse processUrl(String url) {
        // URL 검증
        validateUrl(url);


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

    private void validateUrl(String url) throws InvalidUrlException {
        try {
            URI uri = new URI(url);
            if (!uri.isAbsolute()) {
                throw new InvalidUrlException("URL이 절대 경로가 아닙니다: " + url);
            }

            URL urlObject = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
            // 사용자 에이전트 설정
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new InvalidUrlException("URL에 접근할 수 없습니다: " + url);
            }

            String contentType = connection.getContentType();
            if (contentType == null || !contentType.startsWith("text/html")) {
                throw new InvalidUrlException("URL이 유효한 웹 페이지가 아닙니다: " + url);
            }
        } catch (MalformedURLException | URISyntaxException e) {
            log.error("URL 형식이 잘못되었습니다: {}", url, e);
            throw new InvalidUrlException("URL 형식이 잘못되었습니다: " + url);
        } catch (IOException e) {
            log.error("URL에 접근하는 중 네트워크 오류가 발생했습니다: {}", url, e);
            throw new InvalidUrlException("URL에 접근하는 중 네트워크 오류가 발생했습니다: " + url);
        }
    }





    public void fetchWebPageInfo(UrlInfo urlInfo) {
        try {
            Document doc = Jsoup.connect(urlInfo.getUrl()).get();
            String title = doc.title();
            String faviconUrl = doc.select("link[rel=icon]").attr("href");

            urlInfo.setTitle(title);
            urlInfo.setFaviconSrc(faviconUrl);
        } catch (Exception e) {
            // 예외 처리
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
