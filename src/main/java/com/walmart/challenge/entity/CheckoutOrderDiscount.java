package com.walmart.challenge.entity;

import com.walmart.challenge.enums.DiscountScope;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "checkout_order_discount")
public class CheckoutOrderDiscount {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private CheckoutOrder order;

    @Column(name = "code")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope")
    private DiscountScope scope;

    @Column(name = "description")
    private String description;

    @Column(name = "amount")
    private BigDecimal amount;
}