package com.example.orzon_example.library;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchasedBookRepository extends JpaRepository<PurchasedBook, Long> {
    List<PurchasedBook> findByUserId(Long userId);
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}
