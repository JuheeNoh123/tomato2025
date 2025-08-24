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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovingSpotService {
    private final KakaoClient kakaoClient;
    private final OpenAIClient openAIClient;
    private final StringRedisTemplate redis;
    private final ObjectMapper om;

    private static final Duration CACHE_TTL = Duration.ofHours(2);

    //유저 별 버전 키 (버전을 올려 장소/경로 캐시를 무효화할 때 사용)
    private static final String USER_REC_VER_KEY = "recs:u:%d:ver";

    //추천 장소 캐시 키 (특정 사용자 + 버전에 대한 추천 장소 리스트 저장)
    private static final String PLACES_KEY_FMT = "recs:places:u:%d:v:%d:q:%s";

    // 통합 카탈로그(해당 ver의 모든 장소)
    private static final String PLACES_CATALOG_KEY_FMT = "recs:places:u:%d:v:%d:all";

    //추천 경로 캐시 키 (특정 사용자 + 버전 + 선호에 따른 산책 경로 저장)
    private static final String ROUTES_KEY_FMT = "recs:routes:u:%d:v:%d";

    // 현재 저장된 pref 해시 - 동일 버전에서 같은 테마/난이도/조건이면 캐시 재사용
    private static final String ROUTES_PREF_HASH_FMT = "recs:routes:u:%d:v:%d:prefhash";

    // 추천 장소 조회
    public List<RecommendRes> getRecommendedPlaces(User user, MovingSpotDTO.RecommendReq req){

        long ver = getUserRecVersion(user.getId());

        // 캐시 키 계산 (요청 지문 + 최신 ver)
        String placeKey = buildPlaceKey(
                user.getId(),
                ver,
                req.getQuery()
        );



        // 2) 캐시 조회
        try {
            String cached = redis.opsForValue().get(placeKey);
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
                        BigDecimal.valueOf(r.getLat()),
                        BigDecimal.valueOf(r.getLng()),
                        r.getScore()
                )).toList();

        try{
            String json = om.writeValueAsString(result);
            //개별 쿼리 결과 저장
            redis.opsForValue().set(placeKey, json, CACHE_TTL);

            //버전별 전체 장소 카탈로그 키 갱신
            String catalogKey = PLACES_CATALOG_KEY_FMT.formatted(user.getId(), ver);

            List<RecommendRes> merged = new ArrayList<>();

            String prev = redis.opsForValue().get(catalogKey);
            if(prev != null && !prev.isBlank()){
                merged.addAll(om.readValue(prev, new TypeReference<Collection<? extends RecommendRes>>() {}));
            }
            merged.addAll(result);

            redis.opsForValue().set(catalogKey, om.writeValueAsString(merged), CACHE_TTL);
        }catch (Exception ignore){}


        return result;
    }

    // 추천 경로 생성
    public WalkCourseRes  recommendCourse(User user, MovingSpotDTO.WalkPref pref){

        long ver = getUserRecVersion(user.getId());
        String catalogKey = PLACES_CATALOG_KEY_FMT.formatted(user.getId(), ver);
        String routeKey = buildRouteKey(user.getId(), ver);

        final String prefHashKey = ROUTES_PREF_HASH_FMT.formatted(user.getId(), ver);

        final String currentHash = buildPrefHash(pref);

        // pref 캐시 확인후 동일하면 경로 캐시 사용
        try {
            String savedHash = redis.opsForValue().get(prefHashKey);
            if (currentHash.equals(savedHash)) {
                String cachedRoute = redis.opsForValue().get(routeKey);
                if (cachedRoute != null && !cachedRoute.isBlank()) {
                    return om.readValue(cachedRoute, new TypeReference<WalkCourseRes>() {});
                }
            }
        } catch (Exception ignore) {}

        // 장소 캐시 조회
        List<RecommendRes> candidates;
        try{
            String cached = redis.opsForValue().get(catalogKey);
            if(cached == null || cached.isBlank()){
                throw new IllegalStateException("추천 장소가 없습니다. 장소 추천을 받아주세요.");
            }
            candidates = om.readValue(cached, new TypeReference<List<RecommendRes>>() {});
        }catch (Exception e){
            throw  new IllegalStateException("추천 장소 캐시 파싱 실패", e);
        }

        final int radius = 1000; // 300~800m 추천
        List<RecommendRes> enriched = new ArrayList<>(candidates);

        for (RecommendRes base : candidates) {

            List<RecommendRes> nearby = kakaoClient.findNearbyPOIs(base, radius);

            // 너무 많아지지 않도록 후보별 상한(예: 3개) + 전체 상한(예: 50개)
            if (nearby != null && !nearby.isEmpty()) {
                enriched.addAll(nearby.stream().limit(3).toList());
                if (enriched.size() > 50) break;
            }
        }

        // 이름+좌표 기준 중복 제거
        enriched = enriched.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                r -> r.getName() + "|" + r.getLat() + "|" + r.getLng(),
                                r -> r,
                                (a,b) -> a
                        ),
                        m -> new ArrayList<>(m.values())
                ));

        //경로 생성
        WalkCourseRes result = openAIClient.generateWalkCourses(enriched, pref);

        // 경로 캐시 저장
        try{
            redis.opsForValue().set(routeKey, om.writeValueAsString(result), CACHE_TTL);
            redis.opsForValue().set(prefHashKey, currentHash, CACHE_TTL);
        }catch (Exception ignore){}

        return result;
    }

    // 경로 캐시 키
    private String buildRouteKey(Long userId, long ver) {
        return ROUTES_KEY_FMT.formatted(userId, ver);
    }

    // 장소 캐시 키
    private String buildPlaceKey(Long userId,  long ver, String query) {
        String qhash = Integer.toHexString((query == null ? "":query.trim().toLowerCase()).hashCode());
        return PLACES_KEY_FMT.formatted(userId, ver, qhash);
    }

    // Pref 해시 생성
    private String buildPrefHash(MovingSpotDTO.WalkPref pref) {
        Map<String, Object> hashInput = new LinkedHashMap<>();
        hashInput.put("theme", pref.getTheme());
        hashInput.put("difficulty", pref.getDifficulty());
        hashInput.put("condition", pref.getCondition());
        try {
            String json = om.writeValueAsString(hashInput);
            return Integer.toHexString(json.hashCode());
        } catch (Exception e) {
            return Integer.toHexString(hashInput.hashCode());
        }
    }

    // 유저의 현재 버전 조회
    private long getUserRecVersion(Long userId) {
        String vk = USER_REC_VER_KEY.formatted(userId);
        String v = redis.opsForValue().get(vk);
        if (v == null) {
            redis.opsForValue().set(vk, "1");
            return 1L;
        }
        return Long.parseLong(v);
    }


    @Transactional // 버전 증가
    public void bumpUserRecVersion(Long userId) {
        String vk = USER_REC_VER_KEY.formatted(userId);
        redis.opsForValue().increment(vk);
    }

    //***이거 물어봐야함***
    //이렇게 하면 같은 버전 내에서 서로 다른 테마/난이도 요청이 와도 마지막 요청 결과가 덮어쓰기 됩니다
}
