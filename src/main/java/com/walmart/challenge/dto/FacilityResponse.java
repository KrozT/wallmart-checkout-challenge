package com.walmart.challenge.dto;

import com.walmart.challenge.enums.FacilityType;
import lombok.Data;

@Data
public class FacilityResponse {
    private String id;
    private String name;
    private FacilityType type;
    private ShippingAddressDto logisticAddress;
    private boolean pickupAvailable;
}