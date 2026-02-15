package com.walmart.challenge.repository;

import com.walmart.challenge.entity.ShippingRate;
import com.walmart.challenge.entity.SizeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShippingRateRepository extends JpaRepository<ShippingRate, UUID> {
    /**
     * Retrieves the shipping rate for the given size category.
     *
     * @param sizeCategory the size category
     * @return an optional containing the rate if it exists
     */
    Optional<ShippingRate> findBySizeCategory(SizeCategory sizeCategory);
}