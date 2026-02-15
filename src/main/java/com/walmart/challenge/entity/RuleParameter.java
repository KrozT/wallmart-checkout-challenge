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
@Table(name = "rule_parameters")
public class RuleParameter {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private PromotionRule rule;

    @Column(name = "param_key", nullable = false)
    private String paramKey;

    @Column(name = "numeric_value", precision = 19, scale = 4)
    private BigDecimal numericValue;

    @Column(name = "string_value")
    private String stringValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_product_id")
    private Product relatedProduct;
}