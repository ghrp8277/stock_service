package com.example.stockservice.repository;

import com.example.common.Favorite;
import com.example.common.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserIdAndStock(Long userId, Stock stock);
    Page<Favorite> findByUserId(Long userId, Pageable pageable);
}
