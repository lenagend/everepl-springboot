package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.UrlInfoResponse;
import com.everepl.evereplspringboot.exceptions.InvalidUrlException;
import com.everepl.evereplspringboot.entity.UrlInfo;
import com.everepl.evereplspringboot.repository.UrlInfoRepository;
import com.everepl.evereplspringboot.specification.UrlInfoSpecification;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UrlInfoService {
    private static final Logger log = LoggerFactory.getLogger(UrlInfoService.class);
    private final UrlInfoRepository urlInfoRepository;

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
            return toDto(existingUrlInfo.get());
        } else {
            // URL 정보가 없으면 새로 수집, 저장 후 반환
            UrlInfo urlInfo = new UrlInfo();

            urlInfo.setUrl(url);

            fetchWebPageInfo(urlInfo);

            urlInfoRepository.save(urlInfo);

            return toDto(urlInfo);
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
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36");
            connection.setRequestMethod("HEAD");

            String contentType = connection.getContentType();
            String contentDisposition = connection.getHeaderField("Content-Disposition");

            // 파일 다운로드를 유도하는 콘텐트 타입 목록
            List<String> downloadInducingTypes = Arrays.asList("application/octet-stream", "application/pdf", "application/zip",
                    "application/msword", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            if (downloadInducingTypes.contains(contentType) || (contentDisposition != null && contentDisposition.contains("attachment"))) {
                throw new InvalidUrlException("URL이 파일 다운로드를 유도합니다: " + url);
            }

        } catch (MalformedURLException | URISyntaxException e) {
            log.error("URL 형식이 잘못되었습니다: {}", url, e);
            throw new InvalidUrlException("URL 형식이 잘못되었습니다: " + url);
        } catch (IOException e) {
            log.error("URL에 접근하는 중 네트워크 오류가 발생했습니다: {}", url, e);
            throw new InvalidUrlException("URL에 접근하는 중 네트워크 오류가 발생했습니다: " + url);
        }
    }


    public void fetchWebPageInfo(UrlInfo urlInfo) throws InvalidUrlException {
        try {
            // Jsoup을 사용하여 2초 타임아웃으로 웹 페이지 정보 가져오기
            String url = urlInfo.getUrl();

            Document doc = Jsoup.connect(url)
                    .timeout(5000)
                    .get();
            String title = doc.title();

            String simplifiedUrl = url.replaceAll("^(http://www\\.|https://www\\.|http://|https://)", "");

            // 예외할 도메인 리스트 정의
            List<String> exceptionDomains = Arrays.asList("www.instagram.com");

            // URL에서 도메인 추출
            String domain = new URI(url).getHost();

            // title이 비어 있거나 null이거나 예외 도메인 리스트에 해당 도메인이 포함되어 있을 경우
            if (title == null || title.trim().isEmpty() || exceptionDomains.contains(domain)) {
                title = simplifiedUrl;
            }

            //파비콘
            Element faviconLink = doc.select("link[rel~=.*icon.*]").first();
            String faviconUrl = "";
            if (faviconLink != null) {
                faviconUrl = faviconLink.attr("abs:href");
            }

            urlInfo.setTitle(title);
            urlInfo.setFaviconSrc(faviconUrl);

        } catch (SocketTimeoutException e) {
            log.error("웹 페이지 정보를 가져오는 데 시간이 초과되었습니다: {}", urlInfo.getUrl(), e);
            throw new InvalidUrlException("웹 페이지 정보를 가져오는 데 시간이 초과되었습니다: " + urlInfo.getUrl());
        } catch (Exception e) {
            log.error("웹 페이지 정보를 추출하는 중 오류 발생: {}", urlInfo.getUrl(), e);
            throw new InvalidUrlException("웹 페이지 정보를 추출하는 중 오류 발생: " + urlInfo.getUrl());
        }
    }


    public UrlInfoResponse getUrlInfoById(Long id) {
        Optional<UrlInfo> urlInfo = urlInfoRepository.findById(id);
        if (urlInfo.isPresent()) {
            UrlInfo foundUrlInfo = urlInfo.get();
            foundUrlInfo.incrementViewCount(); // 조회수 증가 메서드
            updatePopularityScore(foundUrlInfo);
            urlInfoRepository.save(foundUrlInfo); // 변경 사항 저장
            return toDto(foundUrlInfo);
        } else {
            throw new NoSuchElementException("Url정보를 찾을 수 없습니다: " + id);
        }
    }

    public Page<UrlInfoResponse> getUrlInfos(List<String> keywords, Pageable pageable) {
        Specification<UrlInfo> spec = UrlInfoSpecification.hasKeywordInUrl(keywords);
        Page<UrlInfo> urlInfos = urlInfoRepository.findAll(spec, pageable);
        return urlInfos.map(this::toDto);
    }

    //댓글수 증감
    public void updateCommentCount(Long urlInfoId, int increment) {
        UrlInfo urlInfo = urlInfoRepository.findById(urlInfoId)
                .orElseThrow(() -> new NoSuchElementException("URL 정보가 존재하지 않습니다: " + urlInfoId));
        urlInfo.updateCommentCount(increment); // 메서드명과 로직 변경
        urlInfoRepository.save(urlInfo);
    }



    // 인기도 점수 계산 및 저장 (UrlInfo 객체가 이미 조회된 상태)
    @Async
    public void updatePopularityScore(UrlInfo urlInfo) {
        try {
            double score = calculatePopularityScore(urlInfo);
            urlInfo.setPopularityScore(score);
            urlInfoRepository.save(urlInfo);
        } catch (Exception e) {
            // 예외 처리 로직
            log.error("Error updating popularity score for UrlInfo: " + urlInfo.getId(), e);
        }
    }

    @Async
    public void updatePopularityScore(Long urlInfoId) {
        try {
            UrlInfo urlInfo = urlInfoRepository.findById(urlInfoId)
                    .orElseThrow(() -> new NoSuchElementException("URL 정보가 존재하지 않습니다: " + urlInfoId));
            double score = calculatePopularityScore(urlInfo);
            urlInfo.setPopularityScore(score);
            urlInfoRepository.save(urlInfo);
        } catch (Exception e) {
            log.error("Error updating popularity score for UrlInfo: " + urlInfoId, e);
        }
    }

    private double calculatePopularityScore(UrlInfo urlInfo) {
        // 가중치 설정
        double viewWeight = 0.1;   // 조회수에 대한 낮은 가중치
        double likeWeight = 0.6;   // 좋아요에 대한 높은 가중치
        double commentWeight = 0.3; // 댓글에 대한 중간 가중치
        double reportPenalty = 0.4; // 신고에 대한 감점

        // 계산 로직
        return (urlInfo.getViewCount() * viewWeight) +
                (urlInfo.getLikeCount() * likeWeight) +
                (urlInfo.getCommentCount() * commentWeight) -
                (urlInfo.getReportCount() * reportPenalty);
    }

    public UrlInfoResponse toDto(UrlInfo urlInfo) {
        return new UrlInfoResponse(
                urlInfo.getId(),
                urlInfo.getUrl(),
                urlInfo.getTitle(),
                urlInfo.getFaviconSrc(),
                urlInfo.getDescription(),
                urlInfo.getCreatedAt(),
                urlInfo.getUpdatedAt(),
                urlInfo.getUpdatedDate(),
                urlInfo.getViewCount(),
                urlInfo.getCommentCount(),
                urlInfo.getLikeCount(),
                urlInfo.getReportCount()
        );
    }

}
