package com.sku_likelion.Moving_Cash_back.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

public class UserDTO {

    @Data
    public static class CreateUser{
        private String userId;
        private String password;
        private String name;
    }

    @Data
    @AllArgsConstructor
    public static class Login{
        private String userId;
        private String password;
    }
}
