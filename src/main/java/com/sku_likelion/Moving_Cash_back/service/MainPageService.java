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
import java.time.format.DateTimeFormatter;
import java.util.*;

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
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay(); // 다음 날 00:00:00


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startStr = startOfDay.format(formatter);
        String endStr = endOfDay.format(formatter);

        Object[] summaries = (Object[]) summaryRepository.findSummariesNative(
                user.getId(),
                req.getStatus().name(),
                startStr,
                endStr
        );

        double totalCalories = ((Number) summaries[0]).doubleValue();
        double totalDistance = ((Number) summaries[1]).doubleValue();
        long steps = ((Number) summaries[2]).longValue();
        System.out.println(totalCalories);
        System.out.println(totalDistance);
        System.out.println(steps);
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
