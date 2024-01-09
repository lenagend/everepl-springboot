package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.dto.UrlInfoResponse;
import com.everepl.evereplspringboot.exceptions.InvalidUrlException;
import com.everepl.evereplspringboot.entity.UrlInfo;
import com.everepl.evereplspringboot.repository.UrlInfoRepository;
import com.everepl.evereplspringboot.specification.UrlInfoSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

            urlInfo.setUrl(url);

            fetchWebPageInfo(urlInfo);

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
            Document doc = Jsoup.connect(urlInfo.getUrl())
                    .timeout(5000)
                    .get();
            String title = doc.title();

            // title이 비어 있거나 null일 경우 도메인 이름을 사용
            if (title == null || title.trim().isEmpty()) {
                URI uri = new URI(urlInfo.getUrl());
                URL aURL = uri.toURL();
                String domain = aURL.getHost();
                // 도메인에서 'www.' 제거
                domain = domain.replaceFirst("^www.*?\\.", "");
                title = domain;
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
            return convertToDto(urlInfo.get());
        } else {
            throw new NoSuchElementException("Url정보를 찾을 수 없습니다: " + id);
        }
    }

    public Page<UrlInfoResponse> getUrlInfos(List<String> keywords, Pageable pageable) {
        Specification<UrlInfo> spec = UrlInfoSpecification.hasKeywordInUrl(keywords);
        Page<UrlInfo> urlInfos = urlInfoRepository.findAll(spec, pageable);
        return urlInfos.map(this::convertToDto);
    }


    public UrlInfoResponse convertToDto(UrlInfo urlInfo) {
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
