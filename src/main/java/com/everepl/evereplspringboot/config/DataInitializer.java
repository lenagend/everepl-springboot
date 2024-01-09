package com.everepl.evereplspringboot.config;

import com.everepl.evereplspringboot.entity.UrlInfo;
import com.everepl.evereplspringboot.repository.UrlInfoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataInitializer implements CommandLineRunner {

    private final UrlInfoRepository urlInfoRepository;

    public DataInitializer(UrlInfoRepository urlInfoRepository) {
        this.urlInfoRepository = urlInfoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<UrlInfo> urlInfos = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            UrlInfo urlInfo = new UrlInfo();
            urlInfo.setUrl("https://entertain.naver.com/read?oid=241&aid=0003321302&ver=" + i);
            urlInfo.setTitle("‘활동 중단’ 지석진 “저 멀쩡해요” 직접 밝힌 근황…’2023 SBS 연예대상’ 프로듀서상 수상" + i);
            urlInfo.setFaviconSrc("https://ssl.pstatic.net/static.news/image/news/2014/favicon/favicon.ico");

            // 날짜를 일 단위로 설정하기 위해 시간, 분, 초, 나노초를 0으로 설정
            LocalDateTime dateTime = LocalDateTime.now()
                    .minusDays(i / 10)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
            LocalDate updateDate = dateTime.toLocalDate();
            urlInfo.setCreatedAt(dateTime);
            urlInfo.setUpdatedAt(dateTime);
            urlInfo.setUpdatedDate(updateDate);

            // viewCount, commentCount, likeCount를 무작위 값으로 설정
            urlInfo.setViewCount(random.nextInt(100)); // 0에서 99 사이
            urlInfo.setCommentCount(random.nextInt(100));
            urlInfo.setLikeCount(random.nextInt(100));
            urlInfo.setReportCount(random.nextInt(10));

            urlInfos.add(urlInfo);
        }

        for (int i = 0; i < 100; i++) {
            UrlInfo urlInfo = new UrlInfo();
            urlInfo.setUrl("https://www.youtube.com/watch?v=K1X2nRijPeY" + i);
            urlInfo.setTitle("조식미션" + i);
            urlInfo.setFaviconSrc("https://www.youtube.com/s/desktop/375de707/img/favicon_32x32.png");

            // 날짜를 일 단위로 설정하기 위해 시간, 분, 초, 나노초를 0으로 설정
            LocalDateTime dateTime = LocalDateTime.now()
                    .minusDays(i / 10)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
            LocalDate updateDate = dateTime.toLocalDate();
            urlInfo.setCreatedAt(dateTime);
            urlInfo.setUpdatedAt(dateTime);
            urlInfo.setUpdatedDate(updateDate);


            // viewCount, commentCount, likeCount를 무작위 값으로 설정
            urlInfo.setViewCount(random.nextInt(100)); // 0에서 99 사이
            urlInfo.setCommentCount(random.nextInt(100));
            urlInfo.setLikeCount(random.nextInt(100));
            urlInfo.setReportCount(random.nextInt(10));

            urlInfos.add(urlInfo);
        }

        for (int i = 0; i < 100; i++) {
            UrlInfo urlInfo = new UrlInfo();
            urlInfo.setUrl("https://gall.dcinside.com/board/view/?id=dcbest&no=198021" + i);
            urlInfo.setTitle("기안84가 30대를 마무리하면서 느낀점" + i);
            urlInfo.setFaviconSrc("//nstatic.dcinside.com/dc/w/images/logo_icon.ico");

            // 날짜를 일 단위로 설정하기 위해 시간, 분, 초, 나노초를 0으로 설정
            LocalDateTime dateTime = LocalDateTime.now()
                    .minusDays(i / 10)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
            urlInfo.setCreatedAt(dateTime);
            urlInfo.setUpdatedAt(dateTime);


            // viewCount, commentCount, likeCount를 무작위 값으로 설정
            urlInfo.setViewCount(random.nextInt(100)); // 0에서 99 사이
            urlInfo.setCommentCount(random.nextInt(100));
            urlInfo.setLikeCount(random.nextInt(100));
            urlInfo.setReportCount(random.nextInt(10));

            urlInfos.add(urlInfo);
        }



        urlInfoRepository.saveAll(urlInfos);
    }
}
