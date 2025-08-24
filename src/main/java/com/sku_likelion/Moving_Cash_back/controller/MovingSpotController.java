package com.sku_likelion.Moving_Cash_back.controller;

import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.dto.request.MovingSpotDTO;
import com.sku_likelion.Moving_Cash_back.kakao.dto.PlaceResponse;
import com.sku_likelion.Moving_Cash_back.service.MovingSpotService;
import com.sku_likelion.Moving_Cash_back.dto.response.MovingSpotDTO.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/movingspot")
public class MovingSpotController {

    private final MovingSpotService movingSpotService;

    @PostMapping("/places")
    public ResponseEntity<List<RecommendRes>> recommendPlaces(@AuthenticationPrincipal User user, @RequestBody MovingSpotDTO.RecommendReq req){
        List<RecommendRes> places = movingSpotService.getRecommendedPlaces(user, req);

        return ResponseEntity.ok(places);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(@AuthenticationPrincipal User user){
        movingSpotService.bumpUserRecVersion(user.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/courses")
    public ResponseEntity<WalkCourseRes> recommendCourses(@AuthenticationPrincipal User user, @RequestBody MovingSpotDTO.WalkPref pref){
        WalkCourseRes res = movingSpotService.recommendCourse(user, pref);
        return ResponseEntity.ok(res);
    }

}
