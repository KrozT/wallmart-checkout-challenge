package com.walmart.challenge.service;

import com.walmart.challenge.dto.CouponRequest;
import com.walmart.challenge.dto.CouponResponse;
import com.walmart.challenge.entity.Coupon;
import com.walmart.challenge.enums.CouponType;
import com.walmart.challenge.enums.DiscountScope;
import com.walmart.challenge.repository.CouponRepository;
import com.walmart.challenge.rules.DiscountDetail;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service encapsulating the logic for validating, applying, and managing coupon codes.
 * It ensures strict adherence to business rules regarding expiration, usage limits,
 * stackability, and data integrity.
 */
@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    /**
     * Validates a list of coupon codes and returns the applicable Coupon entities.
     * Logic includes normalization, deduplication, expiration checks, and stackability enforcement.
     *
     * @param codes List of raw coupon codes provided by the user.
     * @return A list of valid, applicable Coupon entities.
     */
    public List<Coupon> validateAndGetCoupons(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }

        // Normalize and deduplicate codes
        List<String> normalizedCodes = codes.stream()
                .filter(c -> c != null && !c.isBlank())
                .map(String::trim)
                .map(String::toUpperCase)
                .distinct()
                .toList();

        if (normalizedCodes.isEmpty()) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();

        // Fetch candidates and filter by active status, expiry, and remaining uses
        List<Coupon> validCandidates = couponRepository.findByCodeInIgnoreCase(normalizedCodes).stream()
                .filter(Coupon::isActive)
                .filter(c -> c.getExpiry() == null || c.getExpiry().isAfter(now))
                .filter(c -> c.getRemainingUses() == null || c.getRemainingUses() > 0)
                .toList();

        // Select the first coupon per type based on input priority (preserving insertion order)
        Map<CouponType, Coupon> chosenMap = new LinkedHashMap<>();

        // We iterate through normalized codes to preserve the user's input priority
        for (String code : normalizedCodes) {
            validCandidates.stream()
                    .filter(c -> c.getCode().equalsIgnoreCase(code))
                    .findFirst()
                    .ifPresent(coupon -> chosenMap.putIfAbsent(coupon.getCouponType(), coupon));
        }

        // Apply non-stackable constraint: allow at most one non-stackable coupon
        List<Coupon> finalResult = new ArrayList<>();
        boolean nonStackableFound = false;

        for (Coupon coupon : chosenMap.values()) {
            if (!coupon.isStackable()) {
                if (nonStackableFound) {
                    continue; // Skip subsequent non-stackable coupons
                }
                nonStackableFound = true;
            }
            finalResult.add(coupon);
        }

        return finalResult;
    }

    /**
     * Applies the logic of the selected coupons to the order context.
     *
     * @param coupons                    The list of validated coupons.
     * @param totalAfterPromosAndPayment The subtotal after other discounts have been applied.
     * @param shippingCostReference      A single-element array containing the calculated shipping cost (mutable).
     * @return A list of DiscountDetail objects representing the applied discounts.
     */
    public List<DiscountDetail> applyCoupons(List<Coupon> coupons,
                                             BigDecimal totalAfterPromosAndPayment,
                                             BigDecimal[] shippingCostReference) {
        List<DiscountDetail> details = new ArrayList<>();
        if (coupons == null || coupons.isEmpty()) {
            return details;
        }

        for (Coupon coupon : coupons) {
            String description = coupon.getDescription() != null ? coupon.getDescription() : "Coupon " + coupon.getCode();

            if (coupon.getCouponType() == CouponType.SHIPPING) {
                // Eliminate shipping cost entirely
                BigDecimal currentShipping = shippingCostReference[0];
                if (currentShipping != null && currentShipping.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal appliedAmount = currentShipping.setScale(2, RoundingMode.HALF_UP);

                    details.add(new DiscountDetail(
                            coupon.getCode(),
                            DiscountScope.SHIPPING,
                            description,
                            appliedAmount
                    ));

                    // Update the reference to reflect free shipping
                    shippingCostReference[0] = BigDecimal.ZERO;
                    decrementUsage(coupon);
                }
            } else if (coupon.getCouponType() == CouponType.ORDER) {
                // Reduce order total by percentage or fixed amount
                BigDecimal discountAmount = BigDecimal.ZERO;

                if (coupon.getPercentage() != null) {
                    discountAmount = discountAmount.add(totalAfterPromosAndPayment.multiply(coupon.getPercentage()));
                }
                if (coupon.getAmount() != null) {
                    discountAmount = discountAmount.add(coupon.getAmount());
                }

                discountAmount = discountAmount.setScale(2, RoundingMode.HALF_UP);

                if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
                    details.add(new DiscountDetail(
                            coupon.getCode(),
                            DiscountScope.ORDER,
                            description,
                            discountAmount
                    ));
                    decrementUsage(coupon);
                }
            }
        }
        return details;
    }

    private void decrementUsage(Coupon coupon) {
        if (coupon.getRemainingUses() != null) {
            coupon.setRemainingUses(coupon.getRemainingUses() - 1);
            couponRepository.save(coupon);
        }
    }

    /**
     * Retrieves all available coupons in the system.
     *
     * @return List of coupon responses.
     */
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Retrieves a specific coupon by its unique code.
     *
     * @param code The unique coupon code.
     * @return An Optional containing the coupon details if found.
     */
    public Optional<CouponResponse> getCouponByCode(String code) {
        return couponRepository.findByCodeIgnoreCase(code)
                .map(this::mapToResponse);
    }

    /**
     * Creates a new coupon resource.
     * Enforces uniqueness on the coupon code.
     *
     * @param request The coupon creation data.
     * @return The created coupon details.
     * @throws IllegalArgumentException if the coupon code already exists.
     */
    public CouponResponse createCoupon(CouponRequest request) {
        if (couponRepository.findByCodeIgnoreCase(request.getCode()).isPresent()) {
            throw new IllegalArgumentException("Coupon code already exists");
        }

        var coupon = new Coupon();
        mapToEntity(coupon, request);

        var savedCoupon = couponRepository.save(coupon);
        return mapToResponse(savedCoupon);
    }

    /**
     * Updates an existing coupon.
     * Validates that the new code does not conflict with an existing coupon (other than itself).
     *
     * @param code    The current code of the coupon to update.
     * @param request The updated data.
     * @return An Optional containing the updated coupon, or empty if not found.
     * @throws IllegalArgumentException if the new code conflicts with another existing coupon.
     */
    public Optional<CouponResponse> updateCoupon(String code, CouponRequest request) {
        var existingOpt = couponRepository.findByCodeIgnoreCase(code);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        var existingCoupon = existingOpt.get();

        // Validate code uniqueness if the code is being changed
        boolean isCodeChanged = !existingCoupon.getCode().equalsIgnoreCase(request.getCode());
        if (isCodeChanged && couponRepository.findByCodeIgnoreCase(request.getCode()).isPresent()) {
            throw new IllegalArgumentException("New coupon code is already in use");
        }

        mapToEntity(existingCoupon, request);
        var updatedCoupon = couponRepository.save(existingCoupon);

        return Optional.of(mapToResponse(updatedCoupon));
    }

    /**
     * Deletes a coupon from the system.
     *
     * @param code The code of the coupon to delete.
     * @return true if the coupon was found and deleted, false otherwise.
     */
    public boolean deleteCoupon(String code) {
        var couponOpt = couponRepository.findByCodeIgnoreCase(code);
        if (couponOpt.isEmpty()) {
            return false;
        }

        couponRepository.delete(couponOpt.get());
        return true;
    }
    
    /**
     * Internal helper to map request data to the entity.
     * Handles default values for boolean flags and nullable fields.
     */
    private void mapToEntity(Coupon coupon, CouponRequest req) {
        coupon.setCode(req.getCode());
        coupon.setDescription(req.getDescription());
        coupon.setCouponType(req.getCouponType());
        coupon.setPercentage(req.getPercentage());
        coupon.setAmount(req.getAmount());
        coupon.setRemainingUses(req.getRemainingUses());
        coupon.setExpiry(req.getExpiry());

        // Apply defaults for boolean flags
        coupon.setActive(req.getActive() != null ? req.getActive() : true);
        coupon.setStackable(req.getStackable() != null ? req.getStackable() : true);
    }

    /**
     * Internal helper to map the entity to the API response DTO.
     */
    private CouponResponse mapToResponse(Coupon coupon) {
        var resp = new CouponResponse();
        resp.setCode(coupon.getCode());
        resp.setDescription(coupon.getDescription());
        resp.setCouponType(coupon.getCouponType());
        resp.setPercentage(coupon.getPercentage());
        resp.setAmount(coupon.getAmount());
        resp.setActive(coupon.isActive());
        resp.setStackable(coupon.isStackable());
        resp.setRemainingUses(coupon.getRemainingUses());
        resp.setExpiry(coupon.getExpiry());
        return resp;
    }
}