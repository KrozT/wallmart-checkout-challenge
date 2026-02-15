package com.walmart.challenge.dto;

import lombok.Data;

@Data
public class CartItemDto {
    private String sku;
    private int quantity;
}