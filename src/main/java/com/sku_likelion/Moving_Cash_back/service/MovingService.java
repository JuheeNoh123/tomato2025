package com.sku_likelion.Moving_Cash_back.service;

import com.sku_likelion.Moving_Cash_back.domain.RoutePoint;
import com.sku_likelion.Moving_Cash_back.domain.Session;
import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.dto.request.MovingReqDTO;
import com.sku_likelion.Moving_Cash_back.dto.response.MovingResDTO;
import com.sku_likelion.Moving_Cash_back.exception.InvalidIdException;
import com.sku_likelion.Moving_Cash_back.repository.RoutePointRepository;
import com.sku_likelion.Moving_Cash_back.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class MovingService {
    private final SessionRepository sessionRepository;
    private final RoutePointRepository routePointRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public MovingResDTO.GetPointsDTO getPoints(User user){
        MovingResDTO.GetPointsDTO dto = new MovingResDTO.GetPointsDTO();
        dto.setName(user.getName());
        dto.setPoints(user.getPoint());
        return dto;
    }

    @Transactional
    public MovingResDTO.SessionStartDTO start(User user, MovingReqDTO.StatusDTO dto){
        Session session = new Session(user,dto.getStatus(), LocalDateTime.now());
        Session existsession = sessionRepository.findByUser(user);
        if (existsession != null){
            sessionRepository.delete(existsession);
            // 기존 러닝 통계 삭제
            String redisKey = "running:stats:" + existsession.getId();
            redisTemplate.delete(redisKey);
        }
        Session saveSession = sessionRepository.save(session);
        return new MovingResDTO.SessionStartDTO(saveSession.getId(), saveSession.getStartTime());
    }

    @Transactional
    public void saveRoutePoint(MovingReqDTO.RoutePointDTO dto){
        Session session = sessionRepository.findById(dto.getSessionId()).orElseThrow(()-> new InvalidIdException("존재하지 않는 Id"));
        RoutePoint routePoint = new RoutePoint(session, dto.getLat(),dto.getLng(),dto.getPointIndex(),dto.getStep());
        routePointRepository.save(routePoint);
    }




}
