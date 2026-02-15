package com.walmart.challenge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "checkout_order_line")
public class CheckoutOrderLine {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private CheckoutOrder order;

    @Column
    private String sku;

    @Column
    private Integer quantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column
    private BigDecimal subtotal;
}