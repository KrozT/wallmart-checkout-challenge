package com.walmart.challenge.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class LineItemResponse {
    private String sku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public LineItemResponse() {}

    public LineItemResponse(String sku, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }
}