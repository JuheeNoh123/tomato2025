package com.sku_likelion.Moving_Cash_back.domain;

import com.sku_likelion.Moving_Cash_back.enums.ActivityType;
import com.sku_likelion.Moving_Cash_back.enums.LevelType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
@Entity
public class Challenge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LevelType level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activity;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Long reward;


    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDate createdAt;

    public Challenge(LevelType level, ActivityType activity, String title, Long reward){
        this.level = level;
        this.activity = activity;
        this.title = title;
        this.reward = reward;
        this.createdAt = LocalDate.now(ZoneId.of("Asia/Seoul"));
    }

}
