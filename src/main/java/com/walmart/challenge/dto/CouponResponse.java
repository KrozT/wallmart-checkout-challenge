package com.walmart.challenge.dto;

import com.walmart.challenge.enums.CouponType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
public class CouponResponse {
    private String code;
    private String description;
    private CouponType couponType;
    private BigDecimal percentage;
    private BigDecimal amount;
    private boolean active;
    private boolean stackable;
    private Integer remainingUses;
    private LocalDateTime expiry;
}