package com.everepl.evereplspringboot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


@Configuration
public class CorsConfig {

    private String allowedOrigins = "http://localhost:3000";

    @Bean
    public CorsFilter corsFilter(){
        CorsConfiguration config = new CorsConfiguration();

        // 리액트 애플리케이션의 URL 허용
        config.addAllowedOrigin(allowedOrigins); // 리액트 앱 URL

        // 요청 허용 메서드 설정
        config.addAllowedMethod("*");

        // 요청 허용 헤더 설정
        config.addAllowedHeader("*");

        // 쿠키/인증 정보 포함 여부
        config.setAllowCredentials(true);

        // 모든 경로에 대해 위 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}