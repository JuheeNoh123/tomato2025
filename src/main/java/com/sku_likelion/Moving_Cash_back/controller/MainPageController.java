package com.sku_likelion.Moving_Cash_back.controller;

import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.dto.response.MainPageDTO;
import com.sku_likelion.Moving_Cash_back.dto.request.MainPageDTO.mainPageReq;
import com.sku_likelion.Moving_Cash_back.dto.request.MainPageDTO.pointsAmount;
import com.sku_likelion.Moving_Cash_back.dto.response.UserDTO;
import com.sku_likelion.Moving_Cash_back.service.MainPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MainPageController {

    private final MainPageService mainPageService;

    @PostMapping("/mainPage")
    public ResponseEntity<MainPageDTO.mainPageRes> mainPage(@AuthenticationPrincipal User user, @RequestBody mainPageReq req) {
        MainPageDTO.mainPageRes res =mainPageService.mainPage(user, req);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/addPoints")
    public  ResponseEntity<UserDTO.ResponseUser> addPoints(@AuthenticationPrincipal User user, @RequestBody pointsAmount req) {
        User newuser = mainPageService.addPoints(user.getId(), req.getPoints());
        UserDTO.ResponseUser resp = new UserDTO.ResponseUser();
        resp.setPoints(newuser.getPoint());
        resp.setName(newuser.getName());
        resp.setUserId(newuser.getUserId());
        return ResponseEntity.ok(resp);

    }
}
