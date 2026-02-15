package com.walmart.challenge.rules;

import com.walmart.challenge.entity.PromotionRule;

public interface RuleExecutor {
    /**
     * Returns the key identifying this executor. This must match
     * the implementationKey of {@link PromotionRule}s handled by
     * this executor.
     */
    String getImplementationKey();

    /**
     * Evaluates a condition rule. Implementations should return
     * {@code true} if the given rule holds for the provided cart
     * context. For action rules this method may simply return
     * {@code true} to allow processing to continue.
     */
    boolean evaluateCondition(PromotionRule rule, CartContext context);

    /**
     * Executes an action rule and produces a discount. Only called
     * after all conditions for a promotion have passed. Should return
     * {@code null} or a {@link DiscountDetail} with zero amount if the
     * action does not produce a discount in the given context.
     */
    DiscountDetail executeAction(PromotionRule rule, CartContext context);
}