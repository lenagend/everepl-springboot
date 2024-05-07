package com.everepl.evereplspringboot.config;

import com.everepl.evereplspringboot.entity.Comment;
import com.everepl.evereplspringboot.entity.Target;
import com.everepl.evereplspringboot.entity.UrlInfo;
import com.everepl.evereplspringboot.entity.User;
import com.everepl.evereplspringboot.repository.CommentRepository;
import com.everepl.evereplspringboot.repository.UrlInfoRepository;
import com.everepl.evereplspringboot.repository.UserRepository;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {
    private final Logger logger = LoggerFactory.getLogger("LoggerController 의 로그");

    private final UrlInfoRepository urlInfoRepository;

    private final CommentRepository commentRepository;

    private final UserRepository userRepository;

    public DataInitializer(UrlInfoRepository urlInfoRepository, CommentRepository commentRepository, UserRepository userRepository) {
        this.urlInfoRepository = urlInfoRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        User user = new User();
        user.setName("개발자");
        user.setProvider("kakao");
        user.setProviderId("123");

        User savedUser = userRepository.save(user);

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
            int commentCount = random.nextInt(100);
            urlInfo.setCommentCount(commentCount);
            urlInfo.setLikeCount(random.nextInt(100));
            urlInfo.setReportCount(random.nextInt(10));

            urlInfos.add(urlInfo);

        }

        urlInfos = urlInfoRepository.saveAll(urlInfos);

        for (UrlInfo savedUrlInfo : urlInfos) {
            List<Comment> comments = new ArrayList<>();
            for (int j = 0; j < savedUrlInfo.getCommentCount(); j++) {
                Comment comment = new Comment();
                Target target = new Target();
                target.setTargetId(savedUrlInfo.getId()); // 실제 저장된 UrlInfo의 ID 사용
                target.setType(Target.TargetType.URLINFO);
                comment.setTarget(target);
                comment.setUser(savedUser);
                comment.setText("테스트 댓글입니다: " + j);
                comments.add(comment);
            }
            comments = commentRepository.saveAll(comments);
            List<Comment> comments2 = new ArrayList<>();
            for (Comment savedComment : comments){
                String path = savedComment.getTarget().getTargetId() + "/" + savedComment.getId();
                comments2.add(savedComment);
                logger.error(path);
            }

            commentRepository.saveAll(comments2);
        }


    }
}
