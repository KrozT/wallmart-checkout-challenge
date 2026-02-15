package com.walmart.challenge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product_dimension")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDimension {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", unique = true)
    private Product product;

    @Column(name = "height", precision = 19, scale = 4, nullable = false)
    private BigDecimal height;

    @Column(name = "width", precision = 19, scale = 4, nullable = false)
    private BigDecimal width;

    @Column(name = "depth", precision = 19, scale = 4, nullable = false)
    private BigDecimal depth;
}