package com.sku_likelion.Moving_Cash_back.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

public class MovingSpotDTO {

    @Data
    @AllArgsConstructor
    public static class RecommendReq{
        @NotBlank
        private String query;
        @NotNull
        private BigDecimal lat;
        @NotNull
        private BigDecimal lng;
        @NotNull
        private Integer radius;
        @NotNull
        private Integer topK;
        @NotNull
        private Integer page;
    }
}
