package com.sku_likelion.Moving_Cash_back.openai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sku_likelion.Moving_Cash_back.config.OpenAIProperties;
import com.sku_likelion.Moving_Cash_back.openai.dto.ChatRequest;
import com.sku_likelion.Moving_Cash_back.openai.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAIClient {

}
