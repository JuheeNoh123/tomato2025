package com.sku_likelion.Moving_Cash_back.openai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


import java.util.List;
import java.util.Map;

@Data
public class ResponseResponse {
    @JsonProperty("output_text")
    private String output_text;

    private List<Output> output;

    @Data
    public static class Output{
        private String id;
        private String type;
        private String role;
        private List<Content> content;
    }

    @Data
    public static class Content{
        private String type;
        private String text;
    }
}
