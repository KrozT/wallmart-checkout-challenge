package com.walmart.challenge.controller;

import com.walmart.challenge.dto.CheckoutRequest;
import com.walmart.challenge.dto.ConfirmResponse;
import com.walmart.challenge.dto.QuoteResponse;
import com.walmart.challenge.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/checkout")
@RequiredArgsConstructor
@Slf4j
public class CheckoutController {

    private final CheckoutService checkoutService;

    /**
     * Calculates a price quote for the order without persisting it.
     * This is an idempotent operation used to preview costs, shipping, and discounts.
     *
     * @param request the checkout parameters including cart ID and payment method
     * @return the calculated quote
     */
    @PostMapping("/quote")
    public ResponseEntity<QuoteResponse> quote(@Valid @RequestBody CheckoutRequest request) {
        log.info("REST request to calculate quote for cart {}", request.getCartId());

        var response = checkoutService.quote(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Confirms the order and persists it to the database.
     * Returns 201 Created to indicate a new Order resource has been generated.
     *
     * @param request the checkout parameters
     * @return the confirmation details including the generated Order ID
     */
    @PostMapping("/confirm")
    public ResponseEntity<ConfirmResponse> confirm(@Valid @RequestBody CheckoutRequest request) {
        log.info("REST request to confirm order for cart {}", request.getCartId());

        var response = checkoutService.confirm(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}