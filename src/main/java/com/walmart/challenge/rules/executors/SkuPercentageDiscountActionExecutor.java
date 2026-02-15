package com.walmart.challenge.rules.executors;

import com.walmart.challenge.entity.PromotionRule;
import com.walmart.challenge.enums.DiscountScope;
import com.walmart.challenge.rules.CartContext;
import com.walmart.challenge.rules.DiscountDetail;
import com.walmart.challenge.rules.RuleExecutor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Action executor that applies a percentage discount to the subtotal of
 * a specific product in the cart. The rule must have a numeric
 * parameter "percentage" and a product reference parameter "productId".
 */
@Component
public class SkuPercentageDiscountActionExecutor implements RuleExecutor {
    @Override
    public String getImplementationKey() {
        return "SkuPercentageDiscountAction";
    }

    @Override
    public boolean evaluateCondition(PromotionRule rule, CartContext context) {
        return true;
    }

    @Override
    public DiscountDetail executeAction(PromotionRule rule, CartContext context) {
        BigDecimal percentage = rule.getNumericParam("percentage");
        UUID productId = rule.getProductParam("productId");

        if (percentage == null || productId == null) {
            return null;
        }

        BigDecimal base = context.getSubtotalOfProduct(productId);
        BigDecimal amount = base.multiply(percentage).setScale(2, RoundingMode.HALF_UP);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        String code = rule.getPromotion().getCode();
        String description = rule.getPromotion().getDescription();

        // Sku percentage actions apply to a specific item
        return new DiscountDetail(code,
                DiscountScope.ITEM,
                description,
                amount);
    }
}