package com.sku_likelion.Moving_Cash_back.dto.response;

import lombok.Data;

public class UserDTO {

    @Data
    public static class ResponseUser{
        private String userId;
        private String name;
    }
}
