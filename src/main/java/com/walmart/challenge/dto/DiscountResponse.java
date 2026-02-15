package com.walmart.challenge.dto;

import com.walmart.challenge.enums.DiscountScope;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class DiscountResponse {
    private String code;
    private DiscountScope scope;
    private String description;
    private BigDecimal amount;

    public DiscountResponse() {}

    public DiscountResponse(String code, DiscountScope scope, String description, BigDecimal amount) {
        this.code = code;
        this.scope = scope;
        this.description = description;
        this.amount = amount;
    }
}