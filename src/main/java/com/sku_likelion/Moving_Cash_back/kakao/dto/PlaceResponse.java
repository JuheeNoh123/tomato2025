package com.sku_likelion.Moving_Cash_back.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class PlaceResponse {
    private String id;          // kakao place_id

    @JsonProperty("place_name")
    private String name;        // place_name

    @JsonProperty("category_name")
    private String category;    //category_name

    @JsonProperty("road_address_name")
    private String address;     // road_address_name

    @JsonProperty("y")
    private double lat;         // y

    @JsonProperty("x")
    private double lng;         // x

    private Integer score; // 없으면 null
}
