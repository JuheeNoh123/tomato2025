package com.sku_likelion.Moving_Cash_back.repository;

import com.sku_likelion.Moving_Cash_back.domain.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SummaryRepository extends JpaRepository<Summary, Long> {
}
