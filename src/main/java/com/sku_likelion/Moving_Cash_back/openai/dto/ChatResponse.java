package com.sku_likelion.Moving_Cash_back.openai.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatResponse {
    private List<Choice> choices;

    @Data
    public static class Choice{
        private Message message;
    }
    @Data
    public static class Message{
        private String content;
    }
}
