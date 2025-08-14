package com.sku_likelion.Moving_Cash_back.dto.response;

import com.sku_likelion.Moving_Cash_back.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovingResDTO {

    @Data
    public static class GetPointsDTO {
        private String name;
        private Long points;
    }

    @Data
    public static class SessionStartDTO {
        private Long sessionId;
        private LocalDateTime startTime;

        public SessionStartDTO(Long id, LocalDateTime startTime) {
            this.sessionId = id;
            this.startTime = startTime;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RunningStats {
        private double totalDistance; // km
        private double totalCalories; // kcal
        private double pace; // min/km
        private Long points; // 실시간 포인트

    }
}
