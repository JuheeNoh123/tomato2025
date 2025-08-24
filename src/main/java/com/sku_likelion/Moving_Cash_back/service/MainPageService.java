package com.sku_likelion.Moving_Cash_back.service;

import com.sku_likelion.Moving_Cash_back.domain.RoutePoint;
import com.sku_likelion.Moving_Cash_back.domain.Session;
import com.sku_likelion.Moving_Cash_back.domain.Summary;
import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.dto.response.MainPageDTO;
import com.sku_likelion.Moving_Cash_back.dto.request.MainPageDTO.mainPageReq;
import com.sku_likelion.Moving_Cash_back.repository.RoutePointRepository;
import com.sku_likelion.Moving_Cash_back.repository.SessionRepository;
import com.sku_likelion.Moving_Cash_back.repository.SummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MainPageService {

    private final SummaryRepository summaryRepository;
    private final RoutePointRepository routePointRepository;
    private final SessionRepository sessionRepository;


    public MainPageDTO.mainPageRes mainPage(User user, mainPageReq req) {
        MainPageDTO.mainPageRes mainPageRes = new MainPageDTO.mainPageRes();
        mainPageRes.setName(user.getName());
        mainPageRes.setPoints(user.getPoint());
        List<Summary> summaryList = summaryRepository.findByUserAndCreatedAtBetween(user, req.getStartDate(), req.getEndDate());
        Set<LocalDate> resActivityDate = new HashSet<>();
        for (Summary summary : summaryList) {
            resActivityDate.add(summary.getCreatedAt().toLocalDate());
        }
        mainPageRes.setActivateList(resActivityDate);

        LocalDate today = req.getTodayDate().toLocalDate();
        LocalDateTime startOfDay = today.atStartOfDay();                  // 2025-08-16T00:00:00
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);             // 2025-08-16T23:59:59.999999999
        Object[] result = (Object[]) summaryRepository.findTodaySum(user, req.getStatus(), startOfDay, endOfDay);
        double totalCalories = ((Number) result[0]).doubleValue();
        double totalDistance = ((Number) result[1]).doubleValue();
        long steps = ((Number) result[2]).longValue();
        mainPageRes.setTotalCalories(totalCalories);
        mainPageRes.setTotalDistance(totalDistance);
        mainPageRes.setSteps(steps);

        List<MainPageDTO.position> positionList = new ArrayList<>();
        Session session = sessionRepository.findByUser(user);
        List<RoutePoint> routePointList = routePointRepository.findBySession(session);
        for (RoutePoint routePoint : routePointList) {
            MainPageDTO.position position = new MainPageDTO.position();
            position.setLat(routePoint.getLat());
            position.setLng(routePoint.getLng());
            positionList.add(position);

        }
        mainPageRes.setPositionList(positionList);
        return mainPageRes;
    }
}
