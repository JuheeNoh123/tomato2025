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

    @Query(value = "SELECT COALESCE(SUM(total_calories),0) , coalesce(SUM(total_distance),0) , coalesce(SUM(steps),0)  FROM summary " +
            "WHERE user_id = :userId " +
            "AND status = :status " +
            "AND created_at >= :start " +
            "AND created_at < :end", nativeQuery = true)
    Object findSummariesNative(
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("start") String start,
            @Param("end") String end
    );
    List<Summary> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(User user, LocalDateTime startDate, LocalDateTime endDate);
}
