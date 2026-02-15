package com.walmart.challenge.rules.executors;

import com.walmart.challenge.entity.PromotionRule;
import com.walmart.challenge.rules.CartContext;
import com.walmart.challenge.rules.DiscountDetail;
import com.walmart.challenge.rules.RuleExecutor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Condition executor that verifies whether the cart's subtotal meets
 * a minimum threshold. The rule should define a parameter "minTotal"
 * with a numeric value specifying the required minimum.
 */
@Component
public class MinCartTotalConditionExecutor implements RuleExecutor {
    @Override
    public String getImplementationKey() {
        return "MinCartTotalCondition";
    }

    @Override
    public boolean evaluateCondition(PromotionRule rule, CartContext context) {
        BigDecimal threshold = rule.getNumericParam("minTotal");
        if (threshold == null) {
            return false;
        }

        return context.getSubtotal().compareTo(threshold) >= 0;
    }

    @Override
    public DiscountDetail executeAction(PromotionRule rule, CartContext context) {
        // This is a condition rule; no discount produced.
        return null;
    }
}