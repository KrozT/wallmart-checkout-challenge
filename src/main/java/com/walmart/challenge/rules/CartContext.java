package com.walmart.challenge.rules;

import com.walmart.challenge.enums.PaymentMethod;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Encapsulates the information about a shopping cart that rule executors
 * need to evaluate conditions and compute discounts. It holds the
 * cart identifier, the list of line items with pricing and product IDs,
 * the subtotal (sum of line subtotals) and the selected payment
 * method. Additional helper methods provide aggregated information
 * such as total quantity or quantity of a specific product.
 */
@Getter
public class CartContext {
    private final String cartId;
    private final List<CartLine> lines = new ArrayList<>();
    private final BigDecimal subtotal;
    private final PaymentMethod paymentMethod;

    public CartContext(String cartId, List<CartLine> lines, BigDecimal subtotal, PaymentMethod paymentMethod) {
        this.cartId = cartId;
        this.lines.addAll(lines);
        this.subtotal = subtotal;
        this.paymentMethod = paymentMethod;
    }

    /**
     * Returns the total quantity of items in the cart.
     */
    public int getTotalQuantity() {
        return lines.stream().mapToInt(CartLine::quantity).sum();
    }

    /**
     * Returns the quantity of a specific product (by UUID) in the cart.
     */
    public int getQuantityOfProduct(UUID productId) {
        return lines.stream()
                .filter(line -> productId != null && productId.equals(line.productId()))
                .mapToInt(CartLine::quantity)
                .sum();
    }

    /**
     * Returns the subtotal for a specific product (by UUID) in the cart.
     */
    public BigDecimal getSubtotalOfProduct(UUID productId) {
        return lines.stream()
                .filter(line -> productId != null && productId.equals(line.productId()))
                .map(CartLine::lineSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Represents an individual line in the cart. Contains product
     * identifier, sku, quantity, unit price and the computed
     * line subtotal.
     */
    public record CartLine(UUID productId, String sku, int quantity, BigDecimal unitPrice, BigDecimal lineSubtotal) {
    }
}