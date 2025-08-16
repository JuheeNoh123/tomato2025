package com.sku_likelion.Moving_Cash_back.service;

import com.sku_likelion.Moving_Cash_back.domain.RoutePoint;
import com.sku_likelion.Moving_Cash_back.domain.Session;
import com.sku_likelion.Moving_Cash_back.dto.request.MovingReqDTO;
import com.sku_likelion.Moving_Cash_back.dto.response.MovingResDTO;
import com.sku_likelion.Moving_Cash_back.repository.RoutePointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
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
        String redisKey = "running:stats:" + session.getId();

        // Redis에서 기존 러닝 통계 가져오기 없으면 초기값 설정
        MovingResDTO.RunningStats stats = Objects.requireNonNullElse(
                                            (MovingResDTO.RunningStats) redisTemplate.opsForValue().get(redisKey),
                                            new MovingResDTO.RunningStats(0, 0, 0, 0L));


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


        // 페이스 계산
        // 첫 좌표의 시간(시작 시간) 가져오기
        LocalDateTime startTime = routePointRepository.findTopBySessionOrderByPointIndexAsc(session)
                .map(RoutePoint::getCreatedAt)
                .orElse(dto.getTimestamp());

        // 총 경과 시간 계산 (시작~현재)
        Duration duration = Duration.between(startTime, dto.getTimestamp());
        double totalMinutes = Math.max(duration.toMillis() / 60000.0, 0.001); // 분단위, 최소값 0.001로 나누기 방지
        double distanceKm = stats.getTotalDistance();
        if(distanceKm <= 0) distanceKm = 0.000001; // 최소값

        // 페이스(분/km) = 총 소요 시간(분) / 총 거리(km)
        double pace = totalMinutes / distanceKm;
        stats.setPace(pace);


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

    // 일시정지 후 재개 시 Redis에서 stats 가져오기 가능
    public MovingResDTO.RunningStats getCurrentStats(Session session) {
        String redisKey = "running:stats:" + session.getId();
        return (MovingResDTO.RunningStats) redisTemplate.opsForValue().get(redisKey);
    }
}
