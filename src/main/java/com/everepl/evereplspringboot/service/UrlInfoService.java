package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.UrlInfoResponse;
import com.everepl.evereplspringboot.exceptions.InvalidUrlException;
import com.everepl.evereplspringboot.entity.UrlInfo;
import com.everepl.evereplspringboot.repository.UrlInfoRepository;
import com.everepl.evereplspringboot.specification.UrlInfoSpecification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.*;
import java.security.cert.X509Certificate;
import java.time.Duration;
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

    private void setupTrustAllCerts() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }


    public void fetchWebPageInfo(UrlInfo urlInfo) throws InvalidUrlException {
        WebDriver driver = null;
        try {
            setupTrustAllCerts();

            String url = urlInfo.getUrl();

            // 셀레니움으로 처리할 도메인 리스트 정의
            List<String> seleniumDomains = Arrays.asList("instagram.com", "twitter.com");

            // URL에서 도메인 추출
            String domain = extractDomain(url);

            // 셀레니움을 사용해야 하는 경우
            if (seleniumDomains.contains(domain)) {
                // 셀레니움 WebDriver 설정
                driver = new ChromeDriver();
                driver.get(url);
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5)); // 5초 대기

                // 제목 추출
                String title = driver.getTitle();
                urlInfo.setTitle(title);

                // 파비콘 추출
                WebElement faviconLink = driver.findElement(By.cssSelector("link[rel~='icon']"));
                String faviconUrl = "";
                if (faviconLink != null) {
                    faviconUrl = faviconLink.getAttribute("href");
                }
                urlInfo.setFaviconSrc(faviconUrl);
            } else {
                // 기존 Jsoup 처리
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36")
                        .timeout(5000)
                        .get();
                String title = doc.title();

                Element faviconLink = doc.select("link[rel~=.*icon.*]").first();
                String faviconUrl = "";
                if (faviconLink != null) {
                    faviconUrl = faviconLink.attr("abs:href");
                }
                urlInfo.setTitle(title);
                urlInfo.setFaviconSrc(faviconUrl);
            }
        } catch (SocketTimeoutException e) {
            log.error("웹 페이지 정보를 가져오는 데 시간이 초과되었습니다: {}", urlInfo.getUrl(), e);
            throw new InvalidUrlException("웹 페이지 정보를 가져오는 데 시간이 초과되었습니다: " + urlInfo.getUrl());
        } catch (Exception e) {
            log.error("웹 페이지 정보를 추출하는 중 오류 발생: {}", urlInfo.getUrl(), e);
            throw new InvalidUrlException("웹 페이지 정보를 추출하는 중 오류 발생: " + urlInfo.getUrl());
        } finally {
            if (driver != null) {
                driver.quit(); // 셀레니움 드라이버 종료
            }
        }
    }

    private String extractDomain(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        if (domain == null) return null;

        // 'www' 및 서브도메인 제거
        int index = domain.indexOf('.');
        if (index != -1 && index < domain.lastIndexOf('.')) {
            domain = domain.substring(index + 1);
        }
        return domain;
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

    public Page<UrlInfoResponse> findByIds(List<Long> ids, Pageable pageable) {
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

    //댓글수 조회
    public int getCommentCountForUrlInfo(Long urlInfoId) {
        UrlInfo urlInfo = urlInfoRepository.findById(urlInfoId)
                .orElseThrow(() -> new NoSuchElementException("URL 정보가 존재하지 않습니다: " + urlInfoId));
        return urlInfo.getCommentCount();
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
