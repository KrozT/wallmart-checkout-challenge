package com.walmart.challenge.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "shipping_rate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingRate {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "size_category_id")
    private SizeCategory sizeCategory;

    /**
     * Fixed cost component for this size category in local currency.
     */
    @Column(name = "base_cost", precision = 19, scale = 4, nullable = false)
    private BigDecimal baseCost;

    /**
     * Variable cost per kilometre for this size category. The final
     * shipping charge adds this value multiplied by the distance to
     * the base cost.
     */
    @Column(name = "cost_per_km", precision = 19, scale = 4, nullable = false)
    private BigDecimal costPerKm;
}