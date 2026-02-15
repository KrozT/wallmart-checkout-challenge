package com.walmart.challenge.rules.executors;

import com.walmart.challenge.entity.PromotionRule;
import com.walmart.challenge.rules.CartContext;
import com.walmart.challenge.rules.DiscountDetail;
import com.walmart.challenge.rules.RuleExecutor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Condition executor that checks whether the cart contains a certain
 * product in sufficient quantity. The rule should have a parameter
 * "productId" referencing the product and optionally a numeric
 * parameter "minQuantity" specifying the minimum quantity required.
 */
@Component
public class SkuMatchConditionExecutor implements RuleExecutor {
    @Override
    public String getImplementationKey() {
        return "SkuMatchCondition";
    }

    @Override
    public boolean evaluateCondition(PromotionRule rule, CartContext context) {
        UUID productId = rule.getProductParam("productId");
        if (productId == null) {
            return false;
        }

        int quantity = context.getQuantityOfProduct(productId);
        BigDecimal minQtyParam = rule.getNumericParam("minQuantity");
        int minQty = minQtyParam != null ? minQtyParam.intValue() : 1;
        return quantity >= minQty;
    }

    @Override
    public DiscountDetail executeAction(PromotionRule rule, CartContext context) {
        // This is a condition rule; no discount produced.
        return null;
    }
}