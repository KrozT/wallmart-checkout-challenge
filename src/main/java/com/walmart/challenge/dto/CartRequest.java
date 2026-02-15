package com.walmart.challenge.dto;

import com.walmart.challenge.enums.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CartRequest {
    @NotNull
    private String cartId;

    @NotEmpty
    private List<ItemRequest> items;

    @NotNull
    private ShippingAddressDto shippingAddress;

    @NotNull
    private PaymentMethod paymentMethod;
}