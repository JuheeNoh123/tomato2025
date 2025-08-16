package com.sku_likelion.Moving_Cash_back.service;

import com.sku_likelion.Moving_Cash_back.domain.RoutePoint;
import com.sku_likelion.Moving_Cash_back.domain.Session;
import com.sku_likelion.Moving_Cash_back.domain.Summary;
import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.dto.request.MovingReqDTO;
import com.sku_likelion.Moving_Cash_back.dto.response.MovingResDTO;
import com.sku_likelion.Moving_Cash_back.exception.InvalidIdException;
import com.sku_likelion.Moving_Cash_back.repository.RoutePointRepository;
import com.sku_likelion.Moving_Cash_back.repository.SessionRepository;
import com.sku_likelion.Moving_Cash_back.repository.SummaryRepository;
import com.sku_likelion.Moving_Cash_back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class MovingService {
    private final SessionRepository sessionRepository;
    private final RoutePointRepository routePointRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SummaryRepository summaryRepository;
    private final UserRepository userRepository;

    //포인트 + 걸음수 조회
    public MovingResDTO.GetPointAndStepDTO getPointAndStep(User user){
        MovingResDTO.GetPointAndStepDTO dto = new MovingResDTO.GetPointAndStepDTO();
        dto.setName(user.getName());
        dto.setPoint(user.getPoint());
        Session session = sessionRepository.findByUser(user);
        if (session == null) {
            dto.setStep(null);
        }else{
            routePointRepository.findTopBySessionOrderByPointIndexDesc(session).ifPresent(recentPoint -> dto.setStep(recentPoint.getStep()));
        }
        return dto;
    }

    //시작시 세션 저장 : 이미 세션이 있다면 삭제 후 다시 저장 (유저당 세션 1개 / 1:1 관계)
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

    //이동 경로 저장 (세션, 위도, 경도, 총 거리, 포인트 인덱스, 걸음수)
    @Transactional
    public void saveRoutePoint(MovingReqDTO.RoutePointDTO dto){
        Session session = sessionRepository.findById(dto.getSessionId()).orElseThrow(()-> new InvalidIdException("존재하지 않는 Id"));
        RoutePoint routePoint = new RoutePoint(session, dto.getLat(),dto.getLng(), dto.getDistance(), dto.getPointIndex(),dto.getStep());
        routePointRepository.save(routePoint);
    }

    //세션 종료 (달리기/러닝 종료)
    /*로직
    1. 유저 테이블에 포인트 누적
    2. 유저의 시작 세션을 찾아서 종료 시간 삽입 or 업데이트
    3. summary 테이블에 최종 거리, 페이스, 시간, 걸음수, 칼로리, 상태(러닝/워킹) 저장
    4. dto 반환
    */
    @Transactional
    public MovingResDTO.SessionEndDTO end(User user, MovingReqDTO.SessionEndDTO sessionEndReqDTO){
        Long newPoint = user.getPoint()+sessionEndReqDTO.getPoints();
        user.setPoint(newPoint);
        userRepository.save(user);
        Session session = sessionRepository.findByUser(user);
        session.setEndTime(LocalDateTime.now());
        RoutePoint recentPoint = routePointRepository.findTopBySessionOrderByPointIndexDesc(session).orElse(null);
        Summary summary = new Summary(user, sessionEndReqDTO.getTotalCalories(), sessionEndReqDTO.getTotalDistance(), sessionEndReqDTO.getDuration(), recentPoint.getStep(), sessionEndReqDTO.getPace(), session.getStatus());
        summaryRepository.save(summary);

        return new MovingResDTO.SessionEndDTO(summary.getTotalCalories(),summary.getTotalDistance(),summary.getSteps(), summary.getPace(), summary.getTotalTime(), user.getPoint());
    }
}
