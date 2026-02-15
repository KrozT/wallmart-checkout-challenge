package com.walmart.challenge.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
public class ProductResponse {
    private UUID id;
    private String sku;
    private BigDecimal unitPrice;
    private BigDecimal height;
    private BigDecimal width;
    private BigDecimal depth;
    private BigDecimal volume;
    private String sizeCategory;
}