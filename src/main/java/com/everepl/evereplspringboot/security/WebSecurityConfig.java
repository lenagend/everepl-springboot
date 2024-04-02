package com.everepl.evereplspringboot.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    private String allowedOrigins = "http://localhost:3000";

    private CustomOAuth2UserService customOAuth2UserService;

    private final AuthSuccessHandler authSuccessHandler;

    public WebSecurityConfig(CustomOAuth2UserService customOAuth2UserService, AuthSuccessHandler authSuccessHandler) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.authSuccessHandler = authSuccessHandler;
    }

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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf((csrf)->csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(authSuccessHandler) // 인증 성공 핸들러 등록
                        .failureUrl("/loginFailure") // 로그인 실패 시 이동할 URL
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService) // CustomOAuth2UserService를 등록
                        )
                )
        // 모든 요청에 대해 접근 허용
        ;

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}