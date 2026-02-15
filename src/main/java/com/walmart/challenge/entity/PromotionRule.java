package com.walmart.challenge.entity;

import com.walmart.challenge.enums.RuleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "promotion_rules")
public class PromotionRule {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private RuleType ruleType;

    /**
     * The key used to identify which RuleExecutor bean should handle
     * this rule. For example "PercentageDiscountAction" or
     * "MinCartTotalCondition".
     */
    @Column(name = "implementation_key", nullable = false)
    private String implementationKey;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<RuleParameter> parameters = new ArrayList<>();

    /**
     * Helper to get a numeric parameter by key. Returns BigDecimal.ZERO if
     * not present.
     */
    public BigDecimal getNumericParam(String key) {
        return parameters.stream()
            .filter(p -> key.equals(p.getParamKey()))
            .findFirst()
            .map(RuleParameter::getNumericValue)
            .orElse(BigDecimal.ZERO);
    }

    /**
     * Helper to get a string parameter by key. Returns null if not present.
     */
    public String getStringParam(String key) {
        return parameters.stream()
            .filter(p -> key.equals(p.getParamKey()))
            .findFirst()
            .map(RuleParameter::getStringValue)
            .orElse(null);
    }

    /**
     * Helper to get the ID of a related product from a parameter. Returns
     * null if not present.
     */
    public UUID getProductParam(String key) {
        return parameters.stream()
            .filter(p -> key.equals(p.getParamKey()))
            .findFirst()
            .map(p -> p.getRelatedProduct() != null ? p.getRelatedProduct().getId() : null)
            .orElse(null);
    }
}