package com.sku_likelion.Moving_Cash_back.kakao;

import com.sku_likelion.Moving_Cash_back.dto.response.MovingSpotDTO;
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
import java.util.stream.Collectors;

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

    public List<MovingSpotDTO.RecommendRes> findNearbyPOIs(MovingSpotDTO.RecommendRes base, int radius) {
        // 키워드 조합 예: "공원", "산책로", "둘레길", "놀거리", "관광명소"
        String[] keywords = {"공원", "산책로", "둘레길", "소품샵", "관광명소"};
        List<MovingSpotDTO.RecommendRes> out = new ArrayList<>();

        for (String kw : keywords) {
            // Kakao Local: keyword search around base lat/lng
            List<PlaceResponse> rs = searchPlaces(
                    kw,
                    base.getLat(), base.getLng(),
                    radius,
                    1 // 페이지 1 (필요시 2~3페이지만 더)
            );
            if (rs == null) continue;

            for (PlaceResponse p : rs) {
                // base와 너무 가까운/이름 같은 것 등 간단히 중복/잡음 제거
                if (p.getName() == null) continue;
                out.add(new MovingSpotDTO.RecommendRes(
                        base.getUserId(),
                        p.getName(),
                        kw,                       // query에 근거 키워드 남겨두면 추적 쉬움
                        p.getAddress(),
                        BigDecimal.valueOf(p.getLat()),
                        BigDecimal.valueOf(p.getLng()),
                        null // score 없음
                ));
            }
        }

        // 이름/좌표로 중복 제거
        return out.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                r -> (r.getName() + "|" + r.getLat() + "|" + r.getLng()),
                                r -> r,
                                (a, b) -> a
                        ),
                        m -> new ArrayList<>(m.values())
                ));
    }
}