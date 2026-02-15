package com.walmart.challenge.dto;

import lombok.Data;

import java.util.List;

@Data
public class CartDetailsResponse {
    private String cartId;
    private List<CartItemDto> items;
    private ShippingAddressDto shippingAddress;
}