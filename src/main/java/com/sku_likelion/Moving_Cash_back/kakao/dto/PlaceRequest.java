package com.sku_likelion.Moving_Cash_back.kakao.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceRequest {
    @NotBlank
    private String query;

    @NotNull
    private Double lat; //y

    @NotNull
    private Double lng; //x

    @Min(1) @Max(20000)
    private Integer radius;

    @NotBlank
    private String category;

    @Min(1) @Max(45)
    private Integer pages = 3;
}
