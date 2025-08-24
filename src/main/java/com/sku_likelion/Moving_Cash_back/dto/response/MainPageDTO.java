package com.sku_likelion.Moving_Cash_back.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class MainPageDTO {
    /*
    {
	name : "민주",
	activateList : [False, True, False], // startDate~endDate까지 운동 유무 반환
	totalCalories : 546,
	totalDistance : 3.5,
	steps : 729,
	points : 700,
	routePoints:[
			    {"lat": 37.123456, "lng": 127.123456}, //위도, 경도
			    {"lat": 37.123789, "lng": 127.123789},
			    {"lat": 37.124012, "lng": 127.124010},
			    {"lat": 37.124235, "lng": 127.124198},
			    {"lat": 37.124501, "lng": 127.124423},
			    {"lat": 37.124745, "lng": 127.124655},
			    {"lat": 37.124988, "lng": 127.124899},
			    {"lat": 37.125201, "lng": 127.125120},
			    {"lat": 37.125450, "lng": 127.125345},
			    {"lat": 37.125703, "lng": 127.125567}
			]
     }
     */
    @Data
    public static class mainPageRes{
        private String name;
        private List<String> activateList;
        private double totalCalories;
        private double totalDistance;
        private long steps;
        private long points;
        private List<position> positionList;
    }
    @Data
    public static class position{
        private BigDecimal lat;
        private BigDecimal lng;
    }
}
