package com.walmart.challenge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ProductRequest {
    @NotBlank
    private String sku;

    @NotNull
    private BigDecimal unitPrice;

    @NotNull
    private BigDecimal height;

    @NotNull
    private BigDecimal width;

    @NotNull
    private BigDecimal depth;
}