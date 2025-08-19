package com.sku_likelion.Moving_Cash_back.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

public class MovingSpotDTO {

    @Data
    @AllArgsConstructor
    public static class RecommendRes{
        private Long userId;

        private String name;        // 장소 이름

        private String query;       //키워드

        private String address;     // 장소 주소

        private double lat;         // y

        private double lng;         // x

        private Integer score; // 없으면 null
    }
}
