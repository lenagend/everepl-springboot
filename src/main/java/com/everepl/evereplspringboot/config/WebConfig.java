package com.everepl.evereplspringboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///D:/uploaded/images/");

        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        // 그 외의 모든 경로는 API로 처리되도록 설정
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/public/");
    }
}