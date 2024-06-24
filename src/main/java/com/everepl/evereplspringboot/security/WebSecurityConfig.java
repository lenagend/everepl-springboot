package com.everepl.evereplspringboot.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Value("${app.react.url}")
    private String reactAppUrl;

    private CustomOAuth2UserService customOAuth2UserService;

    private final AuthSuccessHandler authSuccessHandler;

    private final JwtTokenFilter jwtTokenFilter;

    public WebSecurityConfig(CustomOAuth2UserService customOAuth2UserService, AuthSuccessHandler authSuccessHandler, JwtTokenFilter jwtTokenFilter) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.authSuccessHandler = authSuccessHandler;
        this.jwtTokenFilter = jwtTokenFilter;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(corsCustomizer()) // CORS 설정 적용
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // OPTIONS 요청 허용
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(authSuccessHandler)
                        .failureUrl("/loginFailure")
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService)
                        )
                )
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS 설정을 위한 Customizer 정의
    private Customizer<CorsConfigurer<HttpSecurity>> corsCustomizer() {
        return cors -> {
            CorsConfigurationSource source = corsConfigurationSource();
            cors.configurationSource(source);
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 리액트 애플리케이션의 URL 허용
        config.addAllowedOrigin(reactAppUrl);

        // 요청 허용 메서드 설정
        config.addAllowedMethod("*");

        // 요청 허용 헤더 설정
        config.addAllowedHeader("*");

        // 쿠키/인증 정보 포함 여부
        config.setAllowCredentials(true);

        // 모든 경로에 대해 위 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

}
