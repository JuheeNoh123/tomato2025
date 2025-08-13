package com.sku_likelion.Moving_Cash_back.controller;

import com.sku_likelion.Moving_Cash_back.dto.request.UserDTO;
import com.sku_likelion.Moving_Cash_back.service.UserService;
import jakarta.validation.Valid;
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
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signUp(@Valid @RequestBody UserDTO.CreateUser req){
        userService.signUp(req);
        UserDTO.Login loginReq = new UserDTO.Login(req.getUserId(), req.getPassword());
        String token = userService.login(loginReq);

        return ResponseEntity.status(HttpStatus.CREATED).body(new TokenResponse("Bearer", token));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody UserDTO.Login req){
        String token = userService.login(req);
        return ResponseEntity.ok(new TokenResponse("Bearer", token));
    }

}
