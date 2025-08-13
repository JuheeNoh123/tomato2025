package com.sku_likelion.Moving_Cash_back.dto.response;

import lombok.Data;

public class MovingDTO {

    @Data
    public static class getPointsDTO {
        private String name;
        private Long points;
    }
}
