package com.walmart.challenge.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCartRequest {
    @NotNull
    @Valid
    private ShippingAddressDto shippingAddress;
}