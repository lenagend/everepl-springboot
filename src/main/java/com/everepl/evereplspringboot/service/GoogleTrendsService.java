package com.everepl.evereplspringboot.service;

import com.everepl.evereplspringboot.entity.TrendingUrl;
import com.everepl.evereplspringboot.entity.UrlInfo;
import com.everepl.evereplspringboot.repository.TrendingUrlRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class GoogleTrendsService {

    private final UrlInfoService urlInfoService;
    private final TrendingUrlRepository trendingUrlRepository;

    public GoogleTrendsService(UrlInfoService urlInfoService, TrendingUrlRepository trendingUrlRepository) {
        this.urlInfoService = urlInfoService;
        this.trendingUrlRepository = trendingUrlRepository;
    }

//    @Scheduled(cron = "0 */2 * * * *") // 2분마다 실행
    @Scheduled(cron = "0 30 3 * * *")
    public void fetchAndProcessTrendingUrls() {
        LocalDate currentDate = LocalDate.now();
        List<TrendingUrl> trendingUrls = trendingUrlRepository.findByDate(currentDate);

        String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));

        for (TrendingUrl trendingUrl : trendingUrls) {
            processSingleTrendsUrl(trendingUrl, formattedDate);
        }
    }

    @Transactional
    public void processSingleTrendsUrl(TrendingUrl trendingUrl, String formattedDate) {
        UrlInfo urlInfo = new UrlInfo();
        urlInfo.setUrl(trendingUrl.getUrl());
        urlInfo.setTitle(formattedDate + " 구글 인기 검색어[" + trendingUrl.getKeyword()+"]");
        urlInfo.setFaviconSrc("https://www.google.com/favicon.ico");
        urlInfoService.saveUrlInfo(urlInfo);
    }

}