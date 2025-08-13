package com.sku_likelion.Moving_Cash_back.dto.request;

import com.sku_likelion.Moving_Cash_back.enums.ActivityType;
import lombok.Data;

public class MovingReqDTO {
    @Data
    public static class statusDTO{
        private ActivityType status;
    }
}
