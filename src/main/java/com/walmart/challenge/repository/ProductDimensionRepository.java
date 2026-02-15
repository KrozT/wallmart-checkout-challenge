package com.walmart.challenge.repository;

import com.walmart.challenge.entity.ProductDimension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductDimensionRepository extends JpaRepository<ProductDimension, UUID> {
    /**
     * Retrieves the dimensions for a given product ID.
     *
     * @param productId the unique identifier of the product
     * @return an optional containing the dimensions if present
     */
    Optional<ProductDimension> findByProductId(UUID productId);

    /**
     * Retrieves dimensions for a collection of product IDs in a single query.
     * This prevents the N+1 Select problem when calculating shipping for
     * carts with multiple items.
     *
     * @param productIds the collection of product unique identifiers
     * @return a list of dimensions matching the provided IDs
     */
    List<ProductDimension> findByProductIdIn(Collection<UUID> productIds);
}