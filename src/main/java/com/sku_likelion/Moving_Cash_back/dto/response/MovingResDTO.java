package com.sku_likelion.Moving_Cash_back.dto.response;

import com.sku_likelion.Moving_Cash_back.enums.ActivityType;
import lombok.Data;

import java.time.LocalDateTime;

public class MovingResDTO {

    @Data
    public static class getPointsDTO {
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
}
