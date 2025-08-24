package com.sku_likelion.Moving_Cash_back.repository;

import com.sku_likelion.Moving_Cash_back.domain.Summary;
import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.enums.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SummaryRepository extends JpaRepository<Summary, Long> {
    List<Summary> findByUserAndCreatedAtBetween(User user, LocalDateTime startDate, LocalDateTime endDate);
    //Optional<Summary> findTopByUserAndStatusAndCreatedAtBetweenOrderByCreatedAtDesc(User user, ActivityType status, LocalDateTime startDate, LocalDateTime endDate);
    @Query("SELECT COALESCE(SUM(s.totalCalories),0), COALESCE(SUM(s.totalDistance),0), COALESCE(SUM(s.steps),0) " +
            "FROM Summary s " +
            "WHERE s.user = :user AND s.status = :status AND s.createdAt BETWEEN :start AND :end")
    Object findTodaySum(@Param("user") User user,
                          @Param("status") ActivityType status,
                          @Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end);
    List<Summary> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(User user, LocalDateTime startDate, LocalDateTime endDate);
}
