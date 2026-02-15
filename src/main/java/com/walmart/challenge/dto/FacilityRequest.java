package com.walmart.challenge.dto;

import com.walmart.challenge.enums.FacilityType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FacilityRequest {

    @NotBlank
    private String name;

    @NotNull
    private FacilityType type;

    @NotNull
    @Valid
    private ShippingAddressDto logisticAddress;

    private boolean pickupAvailable;
}