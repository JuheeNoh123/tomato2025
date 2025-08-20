package com.sku_likelion.Moving_Cash_back.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.dto.request.MovingSpotDTO;
import com.sku_likelion.Moving_Cash_back.kakao.KakaoClient;
import com.sku_likelion.Moving_Cash_back.kakao.dto.PlaceResponse;
import com.sku_likelion.Moving_Cash_back.dto.response.MovingSpotDTO.*;
import com.sku_likelion.Moving_Cash_back.openai.OpenAIClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovingSpotService {
    private final KakaoClient kakaoClient;
    private final OpenAIClient openAIClient;
    private final StringRedisTemplate redis;
    private final ObjectMapper om;

    private static final Duration CACHE_TTL = Duration.ofHours(2);

    private static final String USER_REC_VER_KEY = "recs:u:%d:ver";

    public List<RecommendRes> getRecommendedPlaces(User user, MovingSpotDTO.RecommendReq req){

        long ver = getUserRecVersion(user.getId());

        // 캐시 키 계산 (요청 지문 + 최신 ver)
        String cacheKey = buildKey(
                user.getId(),
                ver
        );

        // 2) 캐시 조회
        try {
            String cached = redis.opsForValue().get(cacheKey);
            if (cached != null && !cached.isBlank()) {
                // 캐시 히트 → 바로 반환
                return om.readValue(cached, new TypeReference<List<RecommendRes>>() {});
            }
        } catch (Exception ignore) {
            // 파싱 실패시 캐시 무시하고 새로 생성
        }

        //후보 조회
        List<PlaceResponse> candidates = kakaoClient.searchPlaces(req.getQuery(), req.getLat(), req.getLng(), req.getRadius(), req.getPage());

        //후보지 없을 경우 예외처리
        if(candidates == null || candidates.isEmpty()){
            throw  new IllegalStateException("주변에 추천 가능한 장소가 없습니다.");
        }

//        for(PlaceResponse p : candidates){
//            System.out.println(p);
//        }

        //OpenAI 추천할 장소 뽑기
        List<PlaceResponse> ranked = openAIClient.rerankPlace(candidates, req.getTopK());

        List<RecommendRes> result = ranked.stream()
                .map(r-> new RecommendRes(
                        user.getId(),
                        r.getName(),
                        req.getQuery(),
                        r.getAddress(),
                        r.getLat(),
                        r.getLng(),
                        r.getScore()
                )).toList();

        try{
            String json = om.writeValueAsString(result);
            redis.opsForValue().set(cacheKey, json, CACHE_TTL);
        }catch (Exception ignore){}


        return result;
    }

    private String buildKey(Long userId,  long ver) {
        return "recs:u:%d:v:%d"
                .formatted(userId, ver);
    }

    private long getUserRecVersion(Long userId) {
        String vk = USER_REC_VER_KEY.formatted(userId);
        String v = redis.opsForValue().get(vk);
        if (v == null) {
            redis.opsForValue().set(vk, "1");
            return 1L;
        }
        return Long.parseLong(v);
    }

    public void bumpUserRecVersion(Long userId) {
        String vk = USER_REC_VER_KEY.formatted(userId);
        redis.opsForValue().increment(vk);
    }

}
