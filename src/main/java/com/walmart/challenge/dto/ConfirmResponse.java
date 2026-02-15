package com.walmart.challenge.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
public class ConfirmResponse {
    private String orderId;
    private String status;
    private String cartId;
    private String currency;
    private List<LineItemResponse> lines;
    private BigDecimal subtotal;
    private List<DiscountResponse> discounts;
    private BigDecimal totalDiscount;
    private BigDecimal total;
    private BigDecimal shippingCost;
    private ShippingAddressDto pickupAddress;
}