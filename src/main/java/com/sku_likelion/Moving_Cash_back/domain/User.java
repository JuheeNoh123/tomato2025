package com.sku_likelion.Moving_Cash_back.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@NoArgsConstructor
@Getter
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String userId;

    private String password;

    @Setter
    private String name;

    public User(String userId, String encodedPassword){
        this.userId = userId;
        this.password = encodedPassword;
    }

    public void changePassword(String encodedPassword){
        this.password = encodedPassword;
    }

}
