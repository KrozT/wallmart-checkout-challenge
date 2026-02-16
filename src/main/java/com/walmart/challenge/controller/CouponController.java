package com.walmart.challenge.controller;

import com.walmart.challenge.dto.CouponRequest;
import com.walmart.challenge.dto.CouponResponse;
import com.walmart.challenge.service.CouponService;
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
 * delegating business logic and persistence to the CouponService.
 */
@RestController
@RequestMapping("/v1/coupons")
@RequiredArgsConstructor
@Slf4j
public class CouponController {

    private final CouponService couponService;

    /**
     * Retrieves all available coupons in the system.
     * The response contains a list of coupons with their current configuration.
     *
     * @return List of coupon responses.
     */
    @GetMapping
    public ResponseEntity<List<CouponResponse>> getAll() {
        return ResponseEntity.ok(couponService.getAllCoupons());
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
        return couponService.getCouponByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new coupon resource.
     * This operation enforces uniqueness on the coupon code via the service layer.
     * If a conflict is detected, a 409 Conflict status is returned.
     * Upon success, a 201 Created status is returned with the Location header.
     *
     * @param request The coupon creation data.
     * @return The created coupon details.
     */
    @PostMapping
    public ResponseEntity<CouponResponse> create(@Valid @RequestBody CouponRequest request) {
        log.info("REST request to create coupon: {}", request.getCode());

        try {
            CouponResponse response = couponService.createCoupon(request);

            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{code}")
                    .buildAndExpand(response.getCode())
                    .toUri();

            return ResponseEntity.created(location).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Attempted to create duplicate coupon code: {}", request.getCode());
            return ResponseEntity.status(409).build();
        }
    }

    /**
     * Updates an existing coupon.
     * This method allows modification of all coupon fields, including the code itself.
     * It validates that the new code does not conflict with an existing coupon.
     *
     * @param code    The current code of the coupon to update.
     * @param request The updated data.
     * @return The updated coupon details.
     */
    @PutMapping("/{code}")
    public ResponseEntity<CouponResponse> update(@PathVariable String code,
                                                 @Valid @RequestBody CouponRequest request) {
        log.info("REST request to update coupon: {}", code);

        try {
            return couponService.updateCoupon(code, request)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (IllegalArgumentException e) {
            log.warn("Conflict detected during update: {}", e.getMessage());
            return ResponseEntity.status(409).build();
        }
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

        if (couponService.deleteCoupon(code)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}