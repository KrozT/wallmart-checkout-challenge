package com.walmart.challenge.rules.executors;

import com.walmart.challenge.entity.PromotionRule;
import com.walmart.challenge.rules.CartContext;
import com.walmart.challenge.rules.DiscountDetail;
import com.walmart.challenge.rules.RuleExecutor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Condition executor that checks if the total quantity of items in the cart
 * meets or exceeds a minimum threshold. The rule should specify a
 * numeric parameter "minQuantity". If not provided, the condition fails.
 */
@Component
public class MinQuantityConditionExecutor implements RuleExecutor {
    @Override
    public String getImplementationKey() {
        return "MinQuantityCondition";
    }

    @Override
    public boolean evaluateCondition(PromotionRule rule, CartContext context) {
        BigDecimal threshold = rule.getNumericParam("minQuantity");
        if (threshold == null) {
            return false;
        }

        int minQty = threshold.intValue();
        return context.getTotalQuantity() >= minQty;
    }

    @Override
    public DiscountDetail executeAction(PromotionRule rule, CartContext context) {
        return null;
    }
}