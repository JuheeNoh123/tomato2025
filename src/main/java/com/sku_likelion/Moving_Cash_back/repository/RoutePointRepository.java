package com.sku_likelion.Moving_Cash_back.repository;

import com.sku_likelion.Moving_Cash_back.domain.RoutePoint;
import com.sku_likelion.Moving_Cash_back.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.nio.channels.FileChannel;
import java.util.Optional;
@Repository
public interface RoutePointRepository extends JpaRepository<RoutePoint, Long> {
    Optional<RoutePoint>  findTopBySessionOrderByPointIndexDesc(Session session);

    Optional<RoutePoint> findTopBySessionOrderByPointIndexAsc(Session session);
}
