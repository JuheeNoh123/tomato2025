package com.sku_likelion.Moving_Cash_back.controller;

import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.dto.request.MovingReqDTO;
import com.sku_likelion.Moving_Cash_back.dto.response.MovingResDTO;
import com.sku_likelion.Moving_Cash_back.service.MovingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/sessions")
public class MovingController {
    private final MovingService movingService;

    @GetMapping("/getPointAndStep")
    public ResponseEntity<MovingResDTO.GetPointAndStepDTO> getPointAndStep(@AuthenticationPrincipal User user) {
        MovingResDTO.GetPointAndStepDTO res = movingService.getPointAndStep(user);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/start")
    public ResponseEntity<MovingResDTO.SessionStartDTO> start(@AuthenticationPrincipal User user, @RequestBody MovingReqDTO.StatusDTO dto) {
        MovingResDTO.SessionStartDTO sessionStartDTO = movingService.start(user,dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionStartDTO);
    }

    @PostMapping("/end")
    public ResponseEntity<MovingResDTO.SessionEndDTO> end(@AuthenticationPrincipal User user, @RequestBody MovingReqDTO.SessionEndDTO dto) {
        MovingResDTO.SessionEndDTO sessionEndDTO = movingService.end(user,dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionEndDTO);
    }
}
