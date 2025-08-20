package com.sku_likelion.Moving_Cash_back.config.external.openai;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;


import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class OpenAIClientConfig {

    private final OpenAIProperties openAIProperties;

    @Bean
    public WebClient openAIWebClient(){
        return WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization","Bearer " + openAIProperties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

}
