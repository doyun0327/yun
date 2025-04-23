package com.example.yun;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class webConfig implements WebMvcConfigurer {
 @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000") // 리액트 개발 서버 주소
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
    
}
