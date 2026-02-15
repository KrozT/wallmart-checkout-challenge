package com.walmart.challenge.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "size_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SizeCategory {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    /**
     * Friendly name of the category (e.g. XS, S, M, L, XL).
     */
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    /**
     * Minimum volume (inclusive) for this category. Volumes are
     * expressed in cubic units. A cart or product with a total
     * volume greater than or equal to this value qualifies for this
     * category.
     */
    @Column(name = "min_volume", precision = 19, scale = 4, nullable = false)
    private BigDecimal minVolume;

    /**
     * Maximum volume (inclusive) for this category. When null this
     * category has no upper bound. A cart or product must have a
     * volume less than or equal to this value to qualify. The
     * combination of minVolume and maxVolume should not overlap with
     * other categories.
     */
    @Column(name = "max_volume", precision = 19, scale = 4)
    private BigDecimal maxVolume;
}