package com.walmart.challenge.dto;

import com.walmart.challenge.enums.CouponType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CouponRequest {
    @NotBlank
    private String code;

    private String description;

    @NotNull
    private CouponType couponType;

    private BigDecimal percentage;
    private BigDecimal amount;
    private Boolean active;
    private Boolean stackable;
    private Integer remainingUses;
    private LocalDateTime expiry;

}