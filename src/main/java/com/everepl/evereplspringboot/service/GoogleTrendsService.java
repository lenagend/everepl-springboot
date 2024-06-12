package com.everepl.evereplspringboot.service;

import org.json.JSONArray;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleTrendsService {

    private final UrlInfoService urlInfoService;

    public GoogleTrendsService(UrlInfoService urlInfoService) {
        this.urlInfoService = urlInfoService;
    }

    @EventListener(ApplicationReadyEvent.class) // 서버 부팅 후 한 번만 실행
    public void fetchTrendingUrlsOnStartup() {
        try {
            fetchAndProcessTrendingUrls();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchAndProcessTrendingUrls() {
        List<String> trendingKeywords = fetchTrendingKeywords();
        List<String> searchUrls = convertKeywordsToGoogleSearchUrls(trendingKeywords);

        // 현재 날짜를 yyyy년 MM월 dd일 형식으로 포맷
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));

        // searchUrls를 순회하면서 키워드와 URL을 매칭
        for (int i = 0; i < searchUrls.size(); i++) {
            String url = searchUrls.get(i);
            String keyword = trendingKeywords.get(i);

            // 각 URL에 대해 사용자 정의 제목 설정
            String title = currentDate + " 구글 일간 검색어 [" + keyword + "]";
            urlInfoService.processTrendUrl(url, title);
        }
    }



    private List<String> fetchTrendingKeywords() {
        List<String> trendingKeywords = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "src/main/resources/get_trends.py");
            pb.environment().put("PYTHONIOENCODING", "UTF-8");  // 파이썬의 출력 인코딩을 UTF-8로 설정
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            // 디버깅 로그: 실제 출력된 내용 확인
            System.out.println("Python script output: " + output.toString());

            // JSON 배열로 파싱
            JSONArray jsonArray = new JSONArray(output.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                trendingKeywords.add(jsonArray.getString(i));
            }

            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 추출된 키워드 리스트 로그로 출력
        System.out.println("Trending Keywords: " + trendingKeywords);

        return trendingKeywords;
    }

    private List<String> convertKeywordsToGoogleSearchUrls(List<String> keywords) {
        List<String> searchUrls = new ArrayList<>();
        String baseUrl = "https://www.google.com/search?q=";

        for (String keyword : keywords) {
            try {
                // 키워드를 URL 인코딩하여 구글 검색 URL 생성
                String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString());
                String searchUrl = baseUrl + encodedKeyword;
                searchUrls.add(searchUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 변환된 검색 URL 리스트 로그로 출력
        System.out.println("Google Search URLs: " + searchUrls);

        return searchUrls;
    }
}