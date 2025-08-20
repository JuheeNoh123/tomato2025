package com.sku_likelion.Moving_Cash_back.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class MovingSpotDTO {

    @Data
    @AllArgsConstructor
    public static class RecommendRes{
        private Long userId;
        private String name;        // 장소 이름
        private String query;       //키워드
        private String address;     // 장소 주소
        private BigDecimal lat;         // y
        private BigDecimal lng;         // x
        private Integer score; // 없으면 null
    }

    @Data
    @AllArgsConstructor
    public static class WalkCourseRes{
        private Long routeId;
        private Node start;
        private List<Node> waypoints;
        private Node destination;
    }
    @Data
    public static class Node{
        private String name;
        private BigDecimal lat;
        private BigDecimal lng;
    }

}
