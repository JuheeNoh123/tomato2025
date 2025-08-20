package com.sku_likelion.Moving_Cash_back.controller;

import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.dto.request.JoinChallengeDTO;
import com.sku_likelion.Moving_Cash_back.service.JoinChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/join")
public class JoinChallengeController {

    private final JoinChallengeService joinChallengeService;

    @PostMapping("/add")
    public ResponseEntity<Void> createJoinChallenge(@AuthenticationPrincipal User user, @RequestBody JoinChallengeDTO.JoinChallengeReq req){
        joinChallengeService.createJoinChallenge(user, req);
        return ResponseEntity.ok().build();
    }

}
