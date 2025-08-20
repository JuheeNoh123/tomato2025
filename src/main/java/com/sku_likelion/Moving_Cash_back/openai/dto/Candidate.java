package com.sku_likelion.Moving_Cash_back.openai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Candidate {
    private String id;
    private String name;
    private String address;
    private String category;
}
