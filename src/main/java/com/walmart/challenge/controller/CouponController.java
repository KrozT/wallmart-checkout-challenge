package com.walmart.challenge.controller;

import com.walmart.challenge.dto.CouponRequest;
import com.walmart.challenge.dto.CouponResponse;
import com.walmart.challenge.entity.Coupon;
import com.walmart.challenge.repository.CouponRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST controller responsible for the lifecycle management of coupons.
 * It provides endpoints to create, retrieve, update, and delete coupons,
 * ensuring strict adherence to unique constraints and data integrity.
 */
@RestController
@RequestMapping("/v1/coupons")
@RequiredArgsConstructor
@Slf4j
public class CouponController {

    private final CouponRepository couponRepository;

    /**
     * Retrieves all available coupons in the system.
     * The response contains a list of coupons with their current configuration.
     *
     * @return List of coupon responses.
     */
    @GetMapping
    public ResponseEntity<List<CouponResponse>> getAll() {
        var coupons = couponRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();

        return ResponseEntity.ok(coupons);
    }

    /**
     * Retrieves a specific coupon by its unique code.
     * The search is case-insensitive.
     *
     * @param code The unique coupon code.
     * @return The coupon details or 404 Not Found.
     */
    @GetMapping("/{code}")
    public ResponseEntity<CouponResponse> getByCode(@PathVariable String code) {
        return couponRepository.findByCodeIgnoreCase(code)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new coupon resource.
     * This operation enforces uniqueness on the coupon code. If a conflict is detected,
     * a 409 Conflict status is returned. Upon success, a 201 Created status
     * is returned with the Location header pointing to the new resource.
     *
     * @param request The coupon creation data.
     * @return The created coupon details.
     */
    @PostMapping
    public ResponseEntity<CouponResponse> create(@Valid @RequestBody CouponRequest request) {
        log.info("REST request to create coupon: {}", request.getCode());

        if (couponRepository.findByCodeIgnoreCase(request.getCode()).isPresent()) {
            log.warn("Attempted to create duplicate coupon code: {}", request.getCode());
            return ResponseEntity.status(409).build();
        }

        var coupon = new Coupon();
        mapToEntity(coupon, request);

        var savedCoupon = couponRepository.save(coupon);
        var response = mapToResponse(savedCoupon);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{code}")
                .buildAndExpand(savedCoupon.getCode())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    /**
     * Updates an existing coupon.
     * This method allows modification of all coupon fields, including the code itself.
     * It validates that the new code does not conflict with an existing coupon
     * other than the one being updated.
     *
     * @param code    The current code of the coupon to update.
     * @param request The updated data.
     * @return The updated coupon details.
     */
    @PutMapping("/{code}")
    public ResponseEntity<CouponResponse> update(@PathVariable String code,
                                                 @Valid @RequestBody CouponRequest request) {
        log.info("REST request to update coupon: {}", code);

        var existingOpt = couponRepository.findByCodeIgnoreCase(code);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var existingCoupon = existingOpt.get();

        // Validate code uniqueness if the code is being changed
        boolean isCodeChanged = !existingCoupon.getCode().equalsIgnoreCase(request.getCode());
        if (isCodeChanged && couponRepository.findByCodeIgnoreCase(request.getCode()).isPresent()) {
            log.warn("Conflict detected: New code {} is already in use", request.getCode());
            return ResponseEntity.status(409).build();
        }

        mapToEntity(existingCoupon, request);
        var updatedCoupon = couponRepository.save(existingCoupon);

        return ResponseEntity.ok(mapToResponse(updatedCoupon));
    }

    /**
     * Deletes a coupon from the system.
     * Returns 204 No Content upon successful deletion.
     *
     * @param code The code of the coupon to delete.
     * @return No Content status or 404 Not Found.
     */
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        log.info("REST request to delete coupon: {}", code);

        var couponOpt = couponRepository.findByCodeIgnoreCase(code);
        if (couponOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        couponRepository.delete(couponOpt.get());
        return ResponseEntity.noContent().build();
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