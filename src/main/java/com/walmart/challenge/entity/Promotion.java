package com.walmart.challenge.entity;

import com.walmart.challenge.enums.RuleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "promotion")
public class Promotion {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    /** Unique code for the promotion (e.g. PROMO_10). */
    @Column(unique = true, nullable = false)
    private String code;

    /** Human-readable name for the promotion. */
    @Column(nullable = false)
    private String name;

    /** Description shown to the user explaining this promotion. */
    @Column
    private String description;

    /** Determines the order in which promotions are evaluated. Lower priority values run first. */
    @Column
    private int priority = 0;

    /** Whether this promotion is active. Only active promotions are processed by the engine. */
    @Column
    private boolean active = true;

    /**
     * Rules belonging to this promotion. Conditions and actions are
     * distinguished by their {@link PromotionRule#getRuleType()}.
     */
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<PromotionRule> rules = new ArrayList<>();

    /**
     * Filters rules to return only conditions.
     */
    public List<PromotionRule> getConditions() {
        return rules.stream()
            .filter(r -> r.getRuleType() == RuleType.CONDITION)
            .toList();
    }

    /**
     * Filters rules to return only actions.
     */
    public List<PromotionRule> getActions() {
        return rules.stream()
            .filter(r -> r.getRuleType() == RuleType.ACTION)
            .toList();
    }
}