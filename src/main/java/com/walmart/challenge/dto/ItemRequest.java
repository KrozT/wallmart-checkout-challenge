package com.walmart.challenge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ItemRequest {
    @NotBlank
    private String sku;

    @Min(1)
    private Integer quantity;
}