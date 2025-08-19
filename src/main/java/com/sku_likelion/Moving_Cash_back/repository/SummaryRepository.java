package com.sku_likelion.Moving_Cash_back.repository;

import com.sku_likelion.Moving_Cash_back.domain.Summary;
import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.enums.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SummaryRepository extends JpaRepository<Summary, Long> {
    List<Summary> findByUserAndCreatedAtBetween(User user, LocalDateTime startDate, LocalDateTime endDate);
    Optional<Summary> findTopByUserAndStatusAndCreatedAtBetweenOrderByCreatedAtDesc(User user, ActivityType status, LocalDateTime startDate, LocalDateTime endDate);
    List<Summary> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(User user, LocalDateTime startDate, LocalDateTime endDate);
}
