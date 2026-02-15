package com.walmart.challenge.service;

import com.walmart.challenge.entity.*;
import com.walmart.challenge.repository.FacilityZoneDistanceRepository;
import com.walmart.challenge.repository.ProductDimensionRepository;
import com.walmart.challenge.repository.ShippingRateRepository;
import com.walmart.challenge.repository.SizeCategoryRepository;
import com.walmart.challenge.rules.CartContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service responsible for calculating shipping costs based on volumetric data
 * and facility-to-zone logistics. It optimizes database interactions by
 * batching dimension retrieval and offloading sorting logic to the persistence layer.
 * All calculations are read-only operations.
 */
@Service
@Slf4j
@Transactional(readOnly = true)
public class ShippingService {

    private final ProductDimensionRepository productDimensionRepository;
    private final SizeCategoryRepository sizeCategoryRepository;
    private final ShippingRateRepository shippingRateRepository;
    private final FacilityZoneDistanceRepository facilityZoneDistanceRepository;

    public ShippingService(ProductDimensionRepository productDimensionRepository,
                           SizeCategoryRepository sizeCategoryRepository,
                           ShippingRateRepository shippingRateRepository,
                           FacilityZoneDistanceRepository facilityZoneDistanceRepository) {
        this.productDimensionRepository = productDimensionRepository;
        this.sizeCategoryRepository = sizeCategoryRepository;
        this.shippingRateRepository = shippingRateRepository;
        this.facilityZoneDistanceRepository = facilityZoneDistanceRepository;
    }

    /**
     * Calculates the shipping cost for a given cart by aggregating product volumes,
     * determining the appropriate size category, and finding the most efficient
     * facility route.
     *
     * @param cart      The cart entity containing the shipping address.
     * @param cartLines The simplified line items containing product IDs and quantities.
     * @return The calculated shipping cost, or ZERO if configuration is missing or invalid.
     */
    public BigDecimal calculateShippingCost(Cart cart, List<CartContext.CartLine> cartLines) {
        if (!isValidShippingAddress(cart)) {
            log.warn("Cart {} has no valid shipping zone. Returning zero cost.", cart.getId());
            return BigDecimal.ZERO;
        }

        String zoneId = cart.getShippingAddress().getZoneId();

        // Aggregate total volumetric weight of all items using batch retrieval
        BigDecimal totalVolume = calculateTotalVolume(cartLines);
        if (totalVolume.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Total volume for cart {} is zero or negative. Skipping calculation.", cart.getId());
            return BigDecimal.ZERO;
        }

        // Determine the applicable size category based on the aggregated volume
        SizeCategory category = determineSizeCategory(totalVolume);
        if (category == null) {
            log.warn("No suitable size category found for volume {}. Defaulting to zero.", totalVolume);
            return BigDecimal.ZERO;
        }

        // Find the nearest facility to the customer's zone to determine distance
        Optional<BigDecimal> distance = findNearestFacilityDistance(zoneId);
        if (distance.isEmpty()) {
            log.warn("No active facility route found for zone {}. Defaulting to zero.", zoneId);
            return BigDecimal.ZERO;
        }

        return calculateFinalCost(category, distance.get());
    }

    private boolean isValidShippingAddress(Cart cart) {
        return cart.getShippingAddress() != null
                && cart.getShippingAddress().getZoneId() != null
                && !cart.getShippingAddress().getZoneId().isBlank();
    }

    /**
     * Calculates total volume by fetching all dimensions in a single query
     * to avoid N+1 Select performance issues.
     */
    private BigDecimal calculateTotalVolume(List<CartContext.CartLine> lines) {
        Set<UUID> productIds = lines.stream()
                .map(CartContext.CartLine::productId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (productIds.isEmpty()) return BigDecimal.ZERO;

        // Bulk fetch dimensions into a Map for O(1) lookup.
        // Assumes repository has: List<ProductDimension> findByProductIdIn(Collection<UUID> ids);
        Map<UUID, ProductDimension> dimensionMap = productDimensionRepository.findByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(dim -> dim.getProduct().getId(), Function.identity()));

        BigDecimal totalVolume = BigDecimal.ZERO;

        for (var line : lines) {
            ProductDimension dim = dimensionMap.get(line.productId());
            if (dim != null) {
                BigDecimal itemVolume = dim.getHeight()
                        .multiply(dim.getWidth())
                        .multiply(dim.getDepth());
                totalVolume = totalVolume.add(itemVolume.multiply(BigDecimal.valueOf(line.quantity())));
            }
        }
        return totalVolume;
    }

    /**
     * Matches the total volume against configured categories.
     * If volume exceeds all defined ranges, it falls back to the largest available category.
     */
    private SizeCategory determineSizeCategory(BigDecimal totalVolume) {
        List<SizeCategory> categories = sizeCategoryRepository.findAllByOrderByMinVolumeAsc();

        for (SizeCategory cat : categories) {
            boolean isAboveMin = totalVolume.compareTo(cat.getMinVolume()) >= 0;
            boolean isBelowMax = cat.getMaxVolume() == null || totalVolume.compareTo(cat.getMaxVolume()) <= 0;

            if (isAboveMin && isBelowMax) {
                return cat;
            }
        }

        if (!categories.isEmpty()) {
            return categories.getLast();
        }

        return null;
    }

    /**
     * Delegates the nearest-facility logic to the database.
     * Returns an Optional to explicitly handle cases where no route exists.
     */
    private Optional<BigDecimal> findNearestFacilityDistance(String zoneId) {
        return facilityZoneDistanceRepository.findTopByZoneIdOrderByDistanceAsc(zoneId)
                .map(FacilityZoneDistance::getDistance);
    }

    private BigDecimal calculateFinalCost(SizeCategory category, BigDecimal distance) {
        return shippingRateRepository.findBySizeCategory(category)
                .map(rate -> {
                    BigDecimal distanceCost = rate.getCostPerKm().multiply(distance);
                    return rate.getBaseCost().add(distanceCost).setScale(2, RoundingMode.HALF_UP);
                })
                .orElseGet(() -> {
                    log.warn("Shipping rate configuration missing for category {}", category.getName());
                    return BigDecimal.ZERO;
                });
    }
}