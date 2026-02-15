package com.walmart.challenge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ShippingAddressDto {
    @NotBlank
    private String street;

    @NotBlank
    private String city;

    @NotBlank
    private String zoneId;
}