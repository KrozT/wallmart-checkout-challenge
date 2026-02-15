package com.walmart.challenge.rules;

import com.walmart.challenge.enums.DiscountScope;

import java.math.BigDecimal;

/**
 * Represents a discount produced by the promotion engine. A discount
 * includes the code of the promotion that generated it, a human-readable description
 * and the absolute amount to subtract from the cart total.
 */
public record DiscountDetail(String code, DiscountScope scope, String description, BigDecimal amount) {
}