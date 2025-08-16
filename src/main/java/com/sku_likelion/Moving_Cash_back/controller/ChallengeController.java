package com.sku_likelion.Moving_Cash_back.controller;

import com.sku_likelion.Moving_Cash_back.domain.Challenge;
import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.dto.response.ChallengeDTO;
import com.sku_likelion.Moving_Cash_back.enums.ActivityType;
import com.sku_likelion.Moving_Cash_back.enums.LevelType;
import com.sku_likelion.Moving_Cash_back.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/challenge")
public class ChallengeController {

    private final ChallengeService challengeService;

//    @PostMapping("/generate")
//    public ResponseEntity<List<Challenge>> generateChallenge(){
//        List<Challenge> challenges =new ArrayList<>();
//        for (ActivityType activity : List.of(ActivityType.WALKING, ActivityType.RUNNING)) {
//            for (LevelType level : LevelType.values()) {
//                challenges= challengeService.generateAndSave(activity, level, 5);
//            }
//        }
//        return ResponseEntity.ok(challenges);
//    }

    @GetMapping("/all/{date}")
    public ResponseEntity<List<ChallengeDTO.ChallengeRes>> getAllChallenge(@AuthenticationPrincipal User user, @PathVariable LocalDate date){
        return ResponseEntity.ok(challengeService.getAllChallenge(user, date));
    }

}
