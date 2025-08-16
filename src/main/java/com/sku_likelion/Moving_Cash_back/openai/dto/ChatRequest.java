package com.sku_likelion.Moving_Cash_back.openai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data @AllArgsConstructor
public class ChatRequest {
    private String model;
    private List<Message> messages;
    private Double temperature;

    @Data @AllArgsConstructor
    public static class Message{
        private String role;
        private String content;
    }
}
