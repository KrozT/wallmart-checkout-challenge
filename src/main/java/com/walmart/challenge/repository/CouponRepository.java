package com.walmart.challenge.repository;

import com.walmart.challenge.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    /**
     * Finds a coupon by its code ignoring case.
     *
     * @param code the coupon code
     * @return an Optional containing the coupon if found
     */
    Optional<Coupon> findByCodeIgnoreCase(String code);

    /**
     * Retrieves all coupons whose codes are contained in the given list.
     * Comparison is case-insensitive.
     *
     * @param codes list of codes to match
     * @return list of matching coupons
     */
    List<Coupon> findByCodeInIgnoreCase(List<String> codes);
}