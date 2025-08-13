package com.sku_likelion.Moving_Cash_back.domain;

import com.sku_likelion.Moving_Cash_back.enums.ActivityType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Entity
public class Session {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    public Session(User user, ActivityType status, LocalDateTime startTime){
        this.user = user;
        this.status = status;
        this.startTime = startTime;
    }

    public void finish(LocalDateTime endTime){
        this.endTime = endTime;
    }

    public boolean isActive(){
        return endTime == null;
    }
}
