package com.sku_likelion.Moving_Cash_back.domain;

import com.sku_likelion.Moving_Cash_back.enums.ActivityType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
@Entity
public class Summary {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "total_calories", nullable = false)
    private Long totalCalories;

    @Column(name = "total_distance", nullable = false, precision = 4, scale = 1)
    private BigDecimal totalDistance;

    @Column(name = "total_time", nullable = false)
    private String totalTime;

    @Column(nullable = false)
    private Long steps;

    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal pace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType status;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public Summary(User user, Long totalCalories, BigDecimal totalDistance, String totalTime, Long steps, BigDecimal pace, ActivityType status){
        this.user = user;
        this.totalCalories = totalCalories;
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
        this.steps = steps;
        this.pace = pace;
        this.status = status;
    }

}
