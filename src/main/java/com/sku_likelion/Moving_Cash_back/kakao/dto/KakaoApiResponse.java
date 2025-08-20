package com.sku_likelion.Moving_Cash_back.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class KakaoApiResponse {

    @JsonProperty("documents")
    private List<PlaceResponse> documents;

    private Meta meta;

    @Data
    public static class Meta{
        @JsonProperty("is_end")
        private boolean isEnd;
    }
}
