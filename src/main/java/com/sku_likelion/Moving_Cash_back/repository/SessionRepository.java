package com.sku_likelion.Moving_Cash_back.repository;

import com.sku_likelion.Moving_Cash_back.domain.Session;
import com.sku_likelion.Moving_Cash_back.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    Session findByUser(User user);
}
