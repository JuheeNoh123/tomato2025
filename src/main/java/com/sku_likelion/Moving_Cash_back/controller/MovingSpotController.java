package com.sku_likelion.Moving_Cash_back.controller;

import com.sku_likelion.Moving_Cash_back.dto.request.MovingSpotDTO;
import com.sku_likelion.Moving_Cash_back.kakao.dto.PlaceResponse;
import com.sku_likelion.Moving_Cash_back.service.MovingSpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/movingspot")
public class MovingSpotController {

    private final MovingSpotService movingSpotService;

    @GetMapping("/places")
    public ResponseEntity<List<PlaceResponse>> recommend(@RequestBody MovingSpotDTO.RecommendReq req){
        List<PlaceResponse> places = movingSpotService.getRecommendedPlaces(req);

        return ResponseEntity.ok(places);
    }
}
