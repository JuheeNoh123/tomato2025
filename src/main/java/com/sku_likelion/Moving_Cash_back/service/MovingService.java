package com.sku_likelion.Moving_Cash_back.service;

import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.dto.response.MovingDTO;
import com.sku_likelion.Moving_Cash_back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class MovingService {


    public MovingDTO.getPointsDTO getPoints(User user){
        MovingDTO.getPointsDTO dto = new MovingDTO.getPointsDTO();
        dto.setName(user.getName());
        dto.setPoints(user.getPoint());
        return dto;
    }
}
