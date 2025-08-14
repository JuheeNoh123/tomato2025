package com.sku_likelion.Moving_Cash_back.dto.request;

import com.sku_likelion.Moving_Cash_back.domain.RoutePoint;
import com.sku_likelion.Moving_Cash_back.domain.Session;
import com.sku_likelion.Moving_Cash_back.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovingReqDTO {
    @Data
    public static class StatusDTO{
        private ActivityType status;
    }

    @Data
    public static class RoutePointDTO {
        private Long sessionId;
        private BigDecimal lat; //위도
        private BigDecimal lng; //경도
        private LocalDateTime timestamp;
        private Long pointIndex;
        private Long step;

        public RoutePointDTO(BigDecimal lat, BigDecimal lng, LocalDateTime timestamp, Long pointIndex, Long step) {
            this.lat = lat;
            this.lng = lng;
            this.timestamp = timestamp;
            this.pointIndex = pointIndex;
            this.step = step;
        }
    }


}
