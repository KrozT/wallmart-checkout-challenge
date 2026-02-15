package com.walmart.challenge.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class QuoteResponse {
    private String cartId;
    private String currency;
    private List<LineItemResponse> lines = new ArrayList<>();
    private BigDecimal subtotal;
    private List<DiscountResponse> discounts = new ArrayList<>();
    private BigDecimal totalDiscount;
    private BigDecimal total;
    private BigDecimal shippingCost;
    private ShippingAddressDto pickupAddress;
}