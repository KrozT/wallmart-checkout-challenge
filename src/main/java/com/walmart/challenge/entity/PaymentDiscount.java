package com.walmart.challenge.entity;

import com.walmart.challenge.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "payment_discount")
public class PaymentDiscount {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", unique = true, nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = true, precision = 5, scale = 4)
    private BigDecimal percentage;

    /**
     * Fixed amount discount (e.g. 1000 CLP off) that applies on top of or
     * instead of the percentage. Nullable: if null no fixed amount discount.
     */
    @Column(name = "amount", nullable = true, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column
    private String description;
}