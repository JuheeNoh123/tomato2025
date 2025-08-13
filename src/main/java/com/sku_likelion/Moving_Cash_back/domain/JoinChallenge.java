package com.sku_likelion.Moving_Cash_back.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(  //컬럼 조합이 유일해야한다는 제약조건 (중복 참여 방지)
                        name = "uk_user_challenge", // 제약 조건 이름
                        columnNames = {"user_id", "challenge_id"} // 조합
                )
        }
)
public class JoinChallenge {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public JoinChallenge(Challenge challenge, User user){
        this.challenge=challenge;
        this.user=user;
    }
}
