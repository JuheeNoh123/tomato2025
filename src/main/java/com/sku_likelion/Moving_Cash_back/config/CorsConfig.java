package com.sku_likelion.Moving_Cash_back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration cfg = new CorsConfiguration();

        // 허용할 출처 지정
        cfg.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:8081",
                "*"
        ));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS")); //허용할 메서드 지정
        cfg.setAllowedHeaders(List.of("*")); //요청 시 보낼 수 있는 HTTP 헤더 지정
        cfg.setAllowCredentials(false); //자격 증명(쿠키, Authorization 헤더 등)을 포함한 요청 허용 여부 / 쿠키 사용 안하면 false
        cfg.setMaxAge(3600L); // prefligh 요청(OPTIONS)의 결과를 캐시하는 시간

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg); // 위 설정을 모든경로에 적용
        return source;
    }
}
