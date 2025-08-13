package com.sku_likelion.Moving_Cash_back.service;

import com.sku_likelion.Moving_Cash_back.domain.Session;
import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.dto.request.MovingReqDTO;
import com.sku_likelion.Moving_Cash_back.dto.response.MovingResDTO;
import com.sku_likelion.Moving_Cash_back.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class MovingService {
    private final SessionRepository sessionRepository;

    public MovingResDTO.getPointsDTO getPoints(User user){
        MovingResDTO.getPointsDTO dto = new MovingResDTO.getPointsDTO();
        dto.setName(user.getName());
        dto.setPoints(user.getPoint());
        return dto;
    }

    @Transactional
    public MovingResDTO.SessionStartDTO start(User user, MovingReqDTO.statusDTO dto){
        Session session = new Session(user,dto.getStatus(), LocalDateTime.now());
        Session saveSession = sessionRepository.save(session);
        return new MovingResDTO.SessionStartDTO(saveSession.getId(), saveSession.getStartTime());
    }
}
