package com.sku_likelion.Moving_Cash_back.openai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@AllArgsConstructor
public class ResponseRequest {
    private String model; //gpt 모델
    private String input; // 시스템 + 유저 프롬프트 합친 문자열
    private List<Map<String, Object>> tools;  // [{"type":"web_search_preview", ...}]
    @JsonProperty("tool_choice")
    private Map<String,Object> tool_choice;   // {"type": "web_search_preview"} 강제 사용시
    private Double temperature;
}
