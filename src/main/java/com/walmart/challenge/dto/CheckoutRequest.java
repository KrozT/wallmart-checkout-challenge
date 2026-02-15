package com.walmart.challenge.dto;

import com.walmart.challenge.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CheckoutRequest {
    @NotBlank
    private String cartId;

    @NotNull
    private PaymentMethod paymentMethod;

    private List<String> couponCodes;
    private String pickupFacilityId;
}