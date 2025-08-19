package com.sku_likelion.Moving_Cash_back.kakao;

import com.sku_likelion.Moving_Cash_back.kakao.dto.KakaoApiResponse;
import com.sku_likelion.Moving_Cash_back.kakao.dto.PlaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KakaoClient {

    private final WebClient kakaoWebClient;

    public List<PlaceResponse> searchPlaces(
            String query, BigDecimal lat, BigDecimal lng, int radius, int pages) {

        List<PlaceResponse> all = new ArrayList<>();
        int maxPages = Math.min(Math.max(pages, 1), 45);

        for (int p = 1; p <= maxPages; p++) {
            final int page = p;

            KakaoApiResponse resp = kakaoWebClient.get()
                    .uri(uri -> uri.path("/v2/local/search/keyword.json")
                            .queryParam("query", query)
                            .queryParam("y", lat)
                            .queryParam("x", lng)
                            .queryParam("radius", radius)
                            .queryParam("size", 15)
                            .queryParam("page", page)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, r ->
                            r.bodyToMono(String.class)
                                    .flatMap(b -> Mono.error(new RuntimeException("Kakao error: " + b))))
                    .bodyToMono(KakaoApiResponse.class)   // << Map 대신 DTO로 바로!
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(300)))
                    .block();

            if (resp == null) break;

            all.addAll(resp.getDocuments());

            if (resp.getMeta() != null && resp.getMeta().isEnd()) {
                break; // 더 이상 페이지 없음
            }
        }

        // id 기준 중복제거 (입력순 유지)
        return all.stream()
                .collect(java.util.stream.Collectors.toMap(
                        PlaceResponse::getId,
                        pr -> pr,
                        (a, b) -> a,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
    }
}