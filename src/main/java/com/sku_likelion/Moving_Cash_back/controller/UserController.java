package com.sku_likelion.Moving_Cash_back.controller;

import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.dto.request.UserDTO;
import com.sku_likelion.Moving_Cash_back.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sku_likelion.Moving_Cash_back.dto.response.UserDTO.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody UserDTO.CreateUser req){
        userService.signUp(req);
        UserDTO.Login loginReq = new UserDTO.Login(req.getUserId(), req.getPassword());
        String token = userService.login(loginReq);

        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDTO.Login req){
        String token = userService.login(req);
        return ResponseEntity.ok(token);
    }

}
