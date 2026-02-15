package com.walmart.challenge.rules;

import com.walmart.challenge.entity.Promotion;
import com.walmart.challenge.entity.PromotionRule;
import com.walmart.challenge.repository.PromotionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PromotionEngine {

    private final PromotionRepository promoRepo;
    private final Map<String, RuleExecutor> executors;

    public PromotionEngine(PromotionRepository promoRepo, List<RuleExecutor> executorList) {
        this.promoRepo = promoRepo;
        this.executors = executorList.stream()
            .collect(Collectors.toMap(RuleExecutor::getImplementationKey, Function.identity()));
    }

    /**
     * Processes the given cart context and returns the list of discounts
     * applicable based on active promotions. Promotions are processed
     * in ascending order of priority, allowing earlier promotions to
     * influence the subtotal used by later ones if needed (not
     * implemented in this example).
     */
    public List<DiscountDetail> process(CartContext context) {
        List<DiscountDetail> applicableDiscounts = new ArrayList<>();
        List<Promotion> activePromos = promoRepo.findByActiveTrueOrderByPriorityAsc();

        log.debug("Processing {} active promotions for cart {}", activePromos.size(), context.getCartId());
        for (Promotion promo : activePromos) {
            boolean conditionsMet = true;
            log.trace("Evaluating promotion {} ({})", promo.getCode(), promo.getName());

            // Evaluate all condition rules
            for (PromotionRule conditionRule : promo.getConditions()) {
                RuleExecutor executor = executors.get(conditionRule.getImplementationKey());
                if (executor == null || !executor.evaluateCondition(conditionRule, context)) {
                    log.trace("Condition {} for promo {} failed", conditionRule.getImplementationKey(), promo.getCode());
                    conditionsMet = false;
                    break;
                }
            }

            // If conditions pass, execute action rules
            if (conditionsMet) {
                log.trace("Conditions satisfied for promo {}", promo.getCode());
                for (PromotionRule actionRule : promo.getActions()) {
                    RuleExecutor executor = executors.get(actionRule.getImplementationKey());
                    if (executor != null) {
                        DiscountDetail detail = executor.executeAction(actionRule, context);
                        if (detail != null && detail.amount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                            applicableDiscounts.add(detail);
                            log.trace("Applied action {} for promo {} amount {}", actionRule.getImplementationKey(), promo.getCode(), detail.amount());
                        }
                    }
                }
            }
        }

        log.debug("Total promo discounts applied: {}", applicableDiscounts.size());
        return applicableDiscounts;
    }
}