package com.sku_likelion.Moving_Cash_back.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

public class UserDTO {

    @Data
    public static class CreateUser{
        @NotBlank(message = "아이디는 필수입니다.")
        private String userId;
        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;
        @NotBlank(message = "이름은 필수입니다.")
        private String name;
    }

    @Data
    @AllArgsConstructor
    public static class Login{
        @NotBlank(message = "아이디는 필수입니다.")
        private String userId;
        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;
    }
}
