package com.sku_likelion.Moving_Cash_back.dto.request;

import com.sku_likelion.Moving_Cash_back.enums.ActivityType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MainPageDTO {
    /*
    *{
	status : RUNNING //or WALKING,
	startDate : "2025-08-10",
	endDate : "2025-08-12"
	todayDate : "2025-08-12"
    }
    * */

    @Data
    public static class mainPageReq{
        private ActivityType status;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private LocalDate todayDate;
    }
}
