package com.sku_likelion.Moving_Cash_back.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sku_likelion.Moving_Cash_back.dto.request.MovingSpotDTO;
import com.sku_likelion.Moving_Cash_back.kakao.KakaoClient;
import com.sku_likelion.Moving_Cash_back.kakao.dto.PlaceResponse;
import com.sku_likelion.Moving_Cash_back.openai.OpenAIClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovingSpotService {
    private final KakaoClient kakaoClient;
    private final OpenAIClient openAIClient;
    private final StringRedisTemplate redis;
    private final ObjectMapper om;

    private static final Duration CACHE_TTL = Duration.ofHours(3);

    public List<PlaceResponse> getRecommendedPlaces(MovingSpotDTO.RecommendReq req){

        String key = buildKey(req.getLat(), req.getLng(), req.getRadius(), req.getTopK());


        List<PlaceResponse> candidates = kakaoClient.searchPlaces(req.getQuery(), req.getLat(), req.getLng(), req.getRadius(), req.getPage());
//        for(PlaceResponse p : candidates){
//            System.out.println(p);
//        }
        List<PlaceResponse> ranked = openAIClient.rerankPlace(candidates, req.getTopK());

        try{
            String json = om.writeValueAsString(ranked);
            redis.opsForValue().set(key, json, CACHE_TTL);
        }catch (Exception ignore){}

        return ranked;
    }

    private String buildKey(BigDecimal lat, BigDecimal lng, int radius, int topK) {
        String latKey = String.format("%.4f", lat);  // 4자리 반올림
        String lngKey = String.format("%.4f", lng);
        return "recs:%d:%s:%s:top%d".formatted( radius, latKey, lngKey, topK);
    }
}
