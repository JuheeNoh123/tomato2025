package com.sku_likelion.Moving_Cash_back.config.external.kakao;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;



@Configuration
@RequiredArgsConstructor
public class KakaoWebClientConfig {

    private final KakaoProperties kakaoProperties;

    @Bean
    public WebClient kakaoWebClient() {
        return WebClient.builder()
                .baseUrl(kakaoProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoProperties.getRestKey())
                .build();
    }
}
