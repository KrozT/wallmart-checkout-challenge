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
 * Action executor that applies a fixed discount per unit of a specific
 * product. The rule must include a numeric parameter "amount" and a
 * product reference parameter "productId". The discount amount is
 * multiplied by the quantity of the product in the cart.
 */
@Component
public class SkuFixedAmountDiscountActionExecutor implements RuleExecutor {
    @Override
    public String getImplementationKey() {
        return "SkuFixedAmountDiscountAction";
    }

    @Override
    public boolean evaluateCondition(PromotionRule rule, CartContext context) {
        return true;
    }

    @Override
    public DiscountDetail executeAction(PromotionRule rule, CartContext context) {
        BigDecimal amount = rule.getNumericParam("amount");
        UUID productId = rule.getProductParam("productId");

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || productId == null) {
            return null;
        }

        int qty = context.getQuantityOfProduct(productId);
        BigDecimal totalAmount = amount.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);

        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        String code = rule.getPromotion().getCode();
        String description = rule.getPromotion().getDescription();

        // Sku fixed amount actions apply to a specific item
        return new DiscountDetail(code,
                DiscountScope.ITEM,
                description,
                totalAmount);
    }
}