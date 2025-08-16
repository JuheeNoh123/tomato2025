package com.sku_likelion.Moving_Cash_back.repository;

import com.sku_likelion.Moving_Cash_back.domain.Challenge;
import com.sku_likelion.Moving_Cash_back.domain.JoinChallenge;
import com.sku_likelion.Moving_Cash_back.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JoinChallengeRepository extends JpaRepository<JoinChallenge, Long> {
    boolean existsByUserAndChallenge(User user, Challenge challenge);
}
