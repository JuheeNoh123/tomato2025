package com.sku_likelion.Moving_Cash_back.dto.request;

import com.sku_likelion.Moving_Cash_back.domain.RoutePoint;
import com.sku_likelion.Moving_Cash_back.domain.Session;
import com.sku_likelion.Moving_Cash_back.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class MovingReqDTO {
    @Data
    public static class StatusDTO{
        private ActivityType status;
    }

    @Data
    public static class RoutePointDTO {
        private Long sessionId;
        private String durationStr; //경과 시간
        private BigDecimal lat; //위도
        private BigDecimal lng; //경도
        private double distance;
        private String timestamp; //utc 형식 시간대 그대로 받기
        private Long pointIndex;
        private Long step;
        public Instant getTimestampUtc() {
            return Instant.parse(this.timestamp); // 문자열 → Instant
        }

        public LocalDateTime getTimestampKst() {
            return LocalDateTime.ofInstant(getTimestampUtc(), ZoneId.of("Asia/Seoul"));
        }

        public RoutePointDTO(BigDecimal lat, BigDecimal lng, double distance,String timestamp, Long pointIndex, Long step, String durationStr) {
            this.lat = lat;
            this.lng = lng;
            this.distance = distance;
            this.timestamp = timestamp;
            this.pointIndex = pointIndex;
            this.step = step;
            this.durationStr = durationStr;
        }
    }

    @Data
    public static class SessionEndDTO{
        private double totalCalories;
        private double totalDistance;
        private double pace;
        private String duration;
        private Long points;
    }

}
