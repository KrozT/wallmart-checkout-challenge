package com.walmart.challenge.rules.executors;

import com.walmart.challenge.entity.PromotionRule;
import com.walmart.challenge.enums.DiscountScope;
import com.walmart.challenge.rules.CartContext;
import com.walmart.challenge.rules.DiscountDetail;
import com.walmart.challenge.rules.RuleExecutor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Action executor that applies a percentage discount to the entire
 * cart subtotal. The rule must specify a numeric parameter
 * "percentage" where 0.10 represents a 10% discount.
 */
@Component
public class PercentageDiscountActionExecutor implements RuleExecutor {
    @Override
    public String getImplementationKey() {
        return "PercentageDiscountAction";
    }

    @Override
    public boolean evaluateCondition(PromotionRule rule, CartContext context) {
        // Actions always return true; conditions are checked separately.
        return true;
    }

    @Override
    public DiscountDetail executeAction(PromotionRule rule, CartContext context) {
        BigDecimal percentage = rule.getNumericParam("percentage");
        if (percentage == null) {
            return null;
        }

        BigDecimal amount = context.getSubtotal().multiply(percentage).setScale(2, RoundingMode.HALF_UP);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        String code = rule.getPromotion().getCode();
        String description = rule.getPromotion().getDescription();

        // Percentage actions apply to the whole order
        return new DiscountDetail(code,
                DiscountScope.ORDER,
                description,
                amount);
    }
}