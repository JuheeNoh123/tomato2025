package com.sku_likelion.Moving_Cash_back.dto.request;

import com.sku_likelion.Moving_Cash_back.enums.ActivityType;
import com.sku_likelion.Moving_Cash_back.enums.LevelType;
import lombok.Data;

import java.time.LocalDate;

public class ChallengeDTO {

    @Data
    public static class ChallengeReq{
        private LevelType level;
        private ActivityType activity;
        private int count;
    }
}
