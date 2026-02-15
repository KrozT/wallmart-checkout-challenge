package com.walmart.challenge.rules.executors;

import com.walmart.challenge.entity.PromotionRule;
import com.walmart.challenge.enums.DiscountScope;
import com.walmart.challenge.rules.CartContext;
import com.walmart.challenge.rules.DiscountDetail;
import com.walmart.challenge.rules.RuleExecutor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Action executor that applies a fixed amount discount to the overall
 * cart. The rule must specify a numeric parameter "amount" denoting
 * the absolute value of the discount. If the amount is zero or
 * negative, no discount is applied.
 */
@Component
public class FixedAmountDiscountActionExecutor implements RuleExecutor {
    @Override
    public String getImplementationKey() {
        return "FixedAmountDiscountAction";
    }

    @Override
    public boolean evaluateCondition(PromotionRule rule, CartContext context) {
        return true;
    }

    @Override
    public DiscountDetail executeAction(PromotionRule rule, CartContext context) {
        BigDecimal amount = rule.getNumericParam("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        String code = rule.getPromotion().getCode();
        String description = rule.getPromotion().getDescription();

        // Fixed amount actions apply to the whole order
        return new DiscountDetail(code,
                DiscountScope.ORDER,
                description,
                amount.setScale(2, java.math.RoundingMode.HALF_UP));
    }
}