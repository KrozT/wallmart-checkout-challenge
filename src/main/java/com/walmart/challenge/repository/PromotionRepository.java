package com.walmart.challenge.repository;

import com.walmart.challenge.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, java.util.UUID> {

    /**
     * Retrieves all promotions that are marked as active, ordered by
     * ascending priority. Higher priority numbers are executed later.
     */
    List<Promotion> findByActiveTrueOrderByPriorityAsc();
}