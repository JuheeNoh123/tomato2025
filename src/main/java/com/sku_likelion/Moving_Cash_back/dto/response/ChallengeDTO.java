package com.sku_likelion.Moving_Cash_back.dto.response;

import com.sku_likelion.Moving_Cash_back.enums.ActivityType;
import com.sku_likelion.Moving_Cash_back.enums.LevelType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

public class ChallengeDTO {

    @AllArgsConstructor
    @Data
    public static class ChallengeRes {
        private Long id;
        private LevelType level;
        private ActivityType activity;
        private String title;
        private Long reward;
        private Boolean status;
    }
}
