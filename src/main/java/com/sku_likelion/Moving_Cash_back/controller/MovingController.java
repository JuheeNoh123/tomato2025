package com.sku_likelion.Moving_Cash_back.controller;

import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.dto.response.MovingDTO;
import com.sku_likelion.Moving_Cash_back.service.MovingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/sessions")
public class MovingController {
    private final MovingService movingService;

    @GetMapping("/getpoints")
    public ResponseEntity<MovingDTO.getPointsDTO> getPoints(@AuthenticationPrincipal User user) {
        MovingDTO.getPointsDTO res = movingService.getPoints(user);
        return ResponseEntity.ok(res);
    }
}
