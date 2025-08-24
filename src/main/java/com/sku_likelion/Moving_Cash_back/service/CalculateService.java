package com.sku_likelion.Moving_Cash_back.service;

import com.sku_likelion.Moving_Cash_back.domain.RoutePoint;
import com.sku_likelion.Moving_Cash_back.domain.Session;
import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.dto.request.MovingReqDTO;
import com.sku_likelion.Moving_Cash_back.dto.response.MovingResDTO;
import com.sku_likelion.Moving_Cash_back.exception.InvalidIdException;
import com.sku_likelion.Moving_Cash_back.repository.RoutePointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CalculateService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RoutePointRepository routePointRepository;

    private static final double CALORIES_PER_KM = 60; // 예시: 60 kcal/km, 체중 무시

    public MovingResDTO.RunningStats updateStats(MovingReqDTO.RoutePointDTO dto, Session session) {
        System.out.println(session.getId());
        String redisKey = "running:stats:" + session.getId();

        // Redis에서 기존 러닝 통계 가져오기 없으면 초기값 설정
        MovingResDTO.RunningStats stats = Objects.requireNonNullElse(
                                            (MovingResDTO.RunningStats) redisTemplate.opsForValue().get(redisKey),
                                            new MovingResDTO.RunningStats(0, 0, 0, 0L, null));


        // DB에서 현재 세션의 마지막 위치 포인트 가져오기 (최근 index 기준)
        RoutePoint lastPoint = routePointRepository.findTopBySessionOrderByPointIndexDesc(session)
                .orElse(null);

        double distance = 0; // 이번 구간 거리

        if(lastPoint != null) {
            // 마지막 좌표와 현재 좌표 간의 거리를 계산 (하버사인 공식)
            distance = calculateDistance(
                    lastPoint.getLat(), lastPoint.getLng(),
                    dto.getLat(), dto.getLng()
            );


        }

        // 총 거리 갱신
        stats.setTotalDistance(stats.getTotalDistance() + distance);
        // 칼로리 = 누적거리 × km당 칼로리 소모량
        stats.setTotalCalories(stats.getTotalCalories() + distance * CALORIES_PER_KM);


        if(lastPoint != null) {
            // 페이스 계산
            double totalMinutes = durationInMinutes(dto.getDurationStr()); // 15.5분
            double distanceKm = stats.getTotalDistance();
            // 페이스(분/km) = 총 소요 시간(분) / 총 거리(km)
            double pace = 0.0;
            if (distanceKm >= 0.1 && totalMinutes >= 0.5) { // 최소 조건 충족 시만 계산
                pace = totalMinutes / distanceKm;
            }
            stats.setPace(pace);
        }


        // 포인트 카운트는 "이전 좌표와 10m 이상 차이"일 때만 증가
        if (distance >= 0.01) { // 0.01 km = 10m
            stats.setPoints(stats.getPoints() + 1);
        }

        // Redis에 갱신된 통계 저장
        redisTemplate.opsForValue().set(redisKey, stats);

        return stats;
    }

    // 하버사인(Haversine) 공식으로 두 GPS 좌표 간 거리(km) 계산
    private double calculateDistance(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        final int R = 6371; // 지구 반지름 (단위: km)
        double latDistance = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double lonDistance = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;// 최종 거리 반환
    }

    // HH:MM:SS 형태 문자열 → Duration
    private static Duration parseDuration(String durationStr) {
        String[] parts = durationStr.split(":");
        if (parts.length != 3) throw new IllegalArgumentException("HH:MM:SS 형식이어야 합니다.");

        long hours = Long.parseLong(parts[0]);
        long minutes = Long.parseLong(parts[1]);
        long seconds = Long.parseLong(parts[2]);

        return Duration.ofHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds);
    }

    // 예시: 총 분으로 변환
    private static double durationInMinutes(String durationStr) {
        Duration duration = parseDuration(durationStr);
        return duration.toSeconds() / 60.0;
    }

}
