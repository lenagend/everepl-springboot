package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.UrlInfoResponse;
import com.everepl.evereplspringboot.entity.BlockedDomain;
import com.everepl.evereplspringboot.exceptions.InvalidUrlException;
import com.everepl.evereplspringboot.entity.UrlInfo;
import com.everepl.evereplspringboot.repository.BlockedDomainRepository;
import com.everepl.evereplspringboot.repository.UrlInfoRepository;
import com.everepl.evereplspringboot.specification.UrlInfoSpecification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
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

    private final BlockedDomainRepository blockedDomainRepository; // 신고된 도메인을 관리하는 저장소


    public UrlInfoService(UrlInfoRepository urlInfoRepository, BlockedDomainRepository blockedDomainRepository) {
        this.urlInfoRepository = urlInfoRepository;
        this.blockedDomainRepository = blockedDomainRepository;
    }

    public void saveUrlInfo(UrlInfo urlInfo){
        urlInfoRepository.save(urlInfo);
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

            // 웹 페이지 정보를 수집
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
            throw new RuntimeException(new InvalidUrlException("URL 형식이 잘못되었습니다: " + url));
        } catch (IOException e) {
            log.error("URL에 접근하는 중 네트워크 오류가 발생했습니다: {}", url, e);
            throw new RuntimeException(new InvalidUrlException("URL에 접근하는 중 네트워크 오류가 발생했습니다: " + url));
        }
    }

    public void fetchWebPageInfo(UrlInfo urlInfo) throws InvalidUrlException {
        try {

            String url = urlInfo.getUrl();

            // URL에서 도메인 추출
            String domain = extractDomain(url);

            urlInfo.setDomain(domain);

            // Jsoup 처리
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36")
                    .timeout(5000)
                    .get();
            String title = doc.title();
            if (title == null || title.isEmpty()) {
                throw new InvalidUrlException("웹 페이지 정보를 추출하는 중 오류 발생: " + urlInfo.getUrl());
            }
            urlInfo.setTitle(title);

            String faviconUrl = "https://" + domain + "/favicon.ico";
            urlInfo.setFaviconSrc(faviconUrl);

        } catch (SocketTimeoutException e) {
            throw new RuntimeException(new InvalidUrlException("웹 페이지 정보를 가져오는 데 시간이 초과되었습니다: " + urlInfo.getUrl()));
        } catch (InvalidUrlException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(new InvalidUrlException("웹 페이지 정보를 추출하는 중 오류 발생: " + urlInfo.getUrl()));
        }
    }


    private String extractDomain(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        if (domain == null) return null;

        // 'www' 제거 (접두사로 있을 경우)
        if (domain.startsWith("www.")) {
            domain = domain.substring(4);
        }

        return domain;
    }

    public UrlInfo getUrlInfoById(Long id) {
        Optional<UrlInfo> urlInfo = urlInfoRepository.findById(id);
        return urlInfo.orElseThrow(() -> new NoSuchElementException("UrlInfo를 찾을수 없습니다: " + id));
    }



    public UrlInfoResponse getUrlInfoResponseById(Long id) {
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

    public Page<UrlInfoResponse> getUrlInfoByIds(List<Long> ids, Pageable pageable) {
        Specification<UrlInfo> spec = new Specification<UrlInfo>() {
            @Override
            public Predicate toPredicate(Root<UrlInfo> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return root.get("id").in(ids);
            }
        };

        return urlInfoRepository.findAll(spec, pageable).map(this::toDto);
    }

    public Page<UrlInfoResponse> getUrlInfos(List<String> keywords, Pageable pageable) {
        Specification<UrlInfo> spec = UrlInfoSpecification.hasKeywordInUrl(keywords);
        Page<UrlInfo> urlInfos = urlInfoRepository.findAll(spec, pageable);
        return urlInfos.map(this::toDto);
    }

    public Page<UrlInfoResponse> searchByTitle(String title, Pageable pageable) {
        Page<UrlInfo> urlInfos = urlInfoRepository.findByTitleContainingIgnoreCase(title, pageable);
        return urlInfos.map(this::toDto);
    }

    //댓글수 증감
    public void updateCommentCount(Long urlInfoId, int increment) {
        UrlInfo urlInfo = urlInfoRepository.findById(urlInfoId)
                .orElseThrow(() -> new NoSuchElementException("URL 정보가 존재하지 않습니다: " + urlInfoId));
        urlInfo.updateCommentCount(increment); // 메서드명과 로직 변경
        urlInfoRepository.save(urlInfo);
    }


    // 증감
    public void updateLikeCount(Long urlInfoId, int increment) {
        UrlInfo urlInfo = urlInfoRepository.findById(urlInfoId)
                .orElseThrow(() -> new NoSuchElementException("URL 정보가 존재하지 않습니다: " + urlInfoId));
        urlInfo.updateLikeCount(increment); // 메서드명과 로직 변경
        updatePopularityScore(urlInfo);
    }

    // 인기도 점수 계산 및 저장 (UrlInfo 객체가 이미 조회된 상태)
    @Async
    public void updatePopularityScore(UrlInfo urlInfo) {
        double score = calculatePopularityScore(urlInfo);
        urlInfo.setPopularityScore(score);
        urlInfoRepository.save(urlInfo);
    }

    @Async
    public void updatePopularityScore(Long urlInfoId) {
        UrlInfo urlInfo = urlInfoRepository.findById(urlInfoId)
                .orElseThrow(() -> new NoSuchElementException("URL 정보가 존재하지 않습니다: " + urlInfoId));
        double score = calculatePopularityScore(urlInfo);
        urlInfo.setPopularityScore(score);
        urlInfoRepository.save(urlInfo);
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

    @Transactional
    public void blockDomain(String url) throws URISyntaxException {
        // URL에서 도메인 추출
        String domain = extractDomain(url);

        // 도메인을 차단 목록에 추가
        BlockedDomain blockedDomain = new BlockedDomain();
        blockedDomain.setDomain(domain);
        blockedDomainRepository.save(blockedDomain);

        // 해당 도메인의 기존 UrlInfo 삭제
        List<UrlInfo> urlInfosToDelete = urlInfoRepository.findByDomain(domain);
        urlInfoRepository.deleteAll(urlInfosToDelete);
    }

    @Transactional
    public void blockUrl(String url) {
        UrlInfo urlInfo = urlInfoRepository.findByUrl(url)
                .orElseThrow(() -> new NoSuchElementException("URL 정보를 찾을 수 없습니다: " + url));

        urlInfo.setBlocked(true); // URL 차단 설정
        urlInfoRepository.save(urlInfo);
    }

    public UrlInfoResponse toDto(UrlInfo urlInfo) {
        String url = urlInfo.getBlocked() ? "https://everepl.com" : urlInfo.getUrl(); // 차단된 URL은 "-" 반환
        String description = urlInfo.getBlocked() ? "정지된 url입니다" : urlInfo.getDescription();

        return new UrlInfoResponse(
                urlInfo.getId(),
                url,
                urlInfo.getTitle(),
                urlInfo.getFaviconSrc(),
                description,
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