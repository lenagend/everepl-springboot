package com.everepl.evereplspringboot.config;

import com.everepl.evereplspringboot.entity.UrlInfo;
import com.everepl.evereplspringboot.repository.UrlInfoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UrlInfoRepository urlInfoRepository;

    public DataInitializer(UrlInfoRepository urlInfoRepository) {
        this.urlInfoRepository = urlInfoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<UrlInfo> urlInfos = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            UrlInfo urlInfo = new UrlInfo();
            urlInfo.setUrl("https://entertain.naver.com/read?oid=241&aid=0003321302&ver=" + i);
            urlInfo.setTitle("‘활동 중단’ 지석진 “저 멀쩡해요” 직접 밝힌 근황…’2023 SBS 연예대상’ 프로듀서상 수상");
            urlInfo.setFaviconSrc("https://ssl.pstatic.net/static.news/image/news/2014/favicon/favicon.ico");
            urlInfo.setDescription("");
            urlInfo.setCreatedAt(LocalDateTime.now());
            urlInfo.setUpdatedAt(LocalDateTime.now());
            urlInfo.setViewCount(i);
            urlInfo.setCommentCount(i);
            urlInfo.setReportCount(i);

            urlInfos.add(urlInfo);
        }

        urlInfoRepository.saveAll(urlInfos);
    }
}
