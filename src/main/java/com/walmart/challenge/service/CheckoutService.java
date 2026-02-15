package com.walmart.challenge.service;

import com.walmart.challenge.dto.*;
import com.walmart.challenge.entity.*;
import com.walmart.challenge.enums.DiscountScope;
import com.walmart.challenge.enums.FulfillmentType;
import com.walmart.challenge.repository.*;
import com.walmart.challenge.rules.CartContext;
import com.walmart.challenge.rules.DiscountDetail;
import com.walmart.challenge.rules.PromotionEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for orchestrating the checkout process.
 * It handles quote calculation and order confirmation/persistence.
 * This service acts as the central coordinator between the cart, pricing rules,
 * shipping logistics, and coupon validations.
 */
@Service
@Slf4j
public class CheckoutService {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final PaymentDiscountRepository paymentDiscountRepository;
    private final CheckoutOrderRepository checkoutOrderRepository;
    private final FacilityRepository facilityRepository;
    private final PromotionEngine promotionEngine;
    private final ShippingService shippingService;
    private final CouponService couponService;

    public CheckoutService(ProductRepository productRepository,
                           CartRepository cartRepository,
                           PaymentDiscountRepository paymentDiscountRepository,
                           CheckoutOrderRepository checkoutOrderRepository,
                           PromotionEngine promotionEngine,
                           ShippingService shippingService,
                           FacilityRepository facilityRepository,
                           CouponService couponService) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.paymentDiscountRepository = paymentDiscountRepository;
        this.checkoutOrderRepository = checkoutOrderRepository;
        this.promotionEngine = promotionEngine;
        this.shippingService = shippingService;
        this.facilityRepository = facilityRepository;
        this.couponService = couponService;
    }

    /**
     * Immutable record to hold the result of a checkout calculation.
     * This acts as the single source of truth for both Quote and Confirm operations,
     * ensuring consistency between the displayed price and the charged amount.
     */
    private record OrderCalculationResult(
            Cart cart,
            List<LineItemResponse> lineItems,
            BigDecimal subtotal,
            List<DiscountDetail> appliedDiscounts,
            BigDecimal totalDiscount,
            BigDecimal shippingCost,
            BigDecimal finalTotal,
            ShippingAddressDto pickupAddress,
            FulfillmentType fulfillmentType
    ) {}

    /**
     * Generates a price quote for the given checkout request.
     * This operation is read-only and does not persist the order.
     * Transactional context is required to allow lazy loading of cart items.
     *
     * @param request The checkout parameters.
     * @return The calculated quote response.
     */
    @Transactional(readOnly = true)
    public QuoteResponse quote(CheckoutRequest request) {
        OrderCalculationResult result = calculateOrderDetails(request);
        return mapToQuoteResponse(result);
    }

    /**
     * Confirms the order, persists it to the database, and returns the confirmation details.
     *
     * @param request The checkout parameters.
     * @return The confirmation response including the generated Order ID.
     */
    @Transactional
    public ConfirmResponse confirm(CheckoutRequest request) {
        log.info("Initiating order confirmation for cart {}", request.getCartId());

        OrderCalculationResult result = calculateOrderDetails(request);
        CheckoutOrder savedOrder = persistOrder(result, request);

        log.info("Order successfully confirmed. ID: {}", savedOrder.getId());

        return mapToConfirmResponse(savedOrder, result);
    }

    /**
     * Core business logic for calculating all financial details of an order.
     * This method centralizes logic to prevent duplication between Quote and Confirm phases.
     */
    private OrderCalculationResult calculateOrderDetails(CheckoutRequest request) {
        // Validate and retrieve the cart to serve as the baseline for the transaction
        UUID cartUuid = UUID.fromString(request.getCartId());
        Cart cart = cartRepository.findById(cartUuid)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + request.getCartId()));

        // Construct line items and calculate the raw subtotal before applying any rules.
        // We re-fetch products to ensure the price is consistent with the current catalog state.
        List<LineItemResponse> lineItems = new ArrayList<>();
        List<CartContext.CartLine> ruleEngineLines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            Product product = productRepository.findBySku(item.getProduct().getSku())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProduct().getSku()));

            BigDecimal lineTotal = product.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            LineItemResponse lir = new LineItemResponse();
            lir.setSku(product.getSku());
            lir.setQuantity(item.getQuantity());
            lir.setUnitPrice(product.getUnitPrice());
            lir.setSubtotal(lineTotal);
            lineItems.add(lir);

            ruleEngineLines.add(new CartContext.CartLine(product.getId(), product.getSku(), item.getQuantity(), product.getUnitPrice(), lineTotal));
        }

        List<DiscountDetail> allDiscounts = new ArrayList<>();
        BigDecimal accumulatedDiscount = BigDecimal.ZERO;

        // Execute the Promotion Engine to apply business rules based on cart content
        CartContext context = new CartContext(request.getCartId(), ruleEngineLines, subtotal, request.getPaymentMethod());
        List<DiscountDetail> promoDiscounts = promotionEngine.process(context);

        for (DiscountDetail dd : promoDiscounts) {
            if (dd.amount().compareTo(BigDecimal.ZERO) > 0) {
                allDiscounts.add(dd);
                accumulatedDiscount = accumulatedDiscount.add(dd.amount());
            }
        }

        // Calculate and apply payment method specific discounts
        BigDecimal totalAfterPromos = subtotal.subtract(accumulatedDiscount);

        paymentDiscountRepository.findByPaymentMethod(request.getPaymentMethod()).ifPresent(pd -> {
            BigDecimal discountAmt = BigDecimal.ZERO;
            if (pd.getPercentage() != null) {
                discountAmt = discountAmt.add(totalAfterPromos.multiply(pd.getPercentage()));
            }
            if (pd.getAmount() != null) {
                discountAmt = discountAmt.add(pd.getAmount());
            }
            discountAmt = discountAmt.setScale(2, RoundingMode.HALF_UP);

            if (discountAmt.compareTo(BigDecimal.ZERO) > 0) {
                allDiscounts.add(new DiscountDetail(
                        pd.getPaymentMethod().name(),
                        DiscountScope.PAYMENT,
                        pd.getDescription(),
                        discountAmt
                ));
            }
        });

        // Recalculate total accumulated discount for downstream logic
        accumulatedDiscount = allDiscounts.stream()
                .map(DiscountDetail::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Determine fulfillment logic (Delivery vs Pickup) and associated shipping costs
        BigDecimal shippingCost = BigDecimal.ZERO;
        ShippingAddressDto pickupAddressDto = null;
        FulfillmentType fulfillmentType = FulfillmentType.DELIVERY;

        if (request.getPickupFacilityId() != null && !request.getPickupFacilityId().isBlank()) {
            UUID facilityId = UUID.fromString(request.getPickupFacilityId());
            Facility facility = facilityRepository.findById(facilityId)
                    .orElseThrow(() -> new IllegalArgumentException("Facility not found: " + request.getPickupFacilityId()));

            if (!facility.isPickupAvailable()) {
                throw new IllegalArgumentException("Selected facility does not support pickup");
            }

            pickupAddressDto = mapToAddressDto(facility.getLogisticAddress());
            fulfillmentType = FulfillmentType.PICKUP;
            // Shipping cost remains ZERO for pickup
        } else {
            shippingCost = shippingService.calculateShippingCost(cart, ruleEngineLines);
        }

        // Apply User Coupons. Note: Coupons may affect the Order Total OR the Shipping Cost.
        // We use a wrapper array to allow the CouponService to modify the shipping cost by reference.
        BigDecimal totalAfterPromosAndPayment = subtotal.subtract(accumulatedDiscount);
        List<Coupon> coupons = couponService.validateAndGetCoupons(request.getCouponCodes());

        BigDecimal[] shippingWrapper = { shippingCost };
        List<DiscountDetail> couponDiscounts = couponService.applyCoupons(coupons, totalAfterPromosAndPayment, shippingWrapper);

        allDiscounts.addAll(couponDiscounts);
        shippingCost = shippingWrapper[0];

        // Compute final total based on all applied logic
        BigDecimal finalTotalDiscount = allDiscounts.stream()
                .map(DiscountDetail::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalTotal = subtotal.subtract(finalTotalDiscount).add(shippingCost)
                .setScale(2, RoundingMode.HALF_UP);

        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalTotal = BigDecimal.ZERO;
        }

        return new OrderCalculationResult(
                cart, lineItems, subtotal, allDiscounts, finalTotalDiscount, shippingCost, finalTotal, pickupAddressDto, fulfillmentType
        );
    }

    private CheckoutOrder persistOrder(OrderCalculationResult result, CheckoutRequest request) {
        CheckoutOrder order = new CheckoutOrder();
        order.setCartId(result.cart().getId().toString());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setFulfillmentType(result.fulfillmentType());
        order.setSubtotal(result.subtotal());
        order.setTotal(result.finalTotal());
        order.setShippingCost(result.shippingCost());
        order.setCreatedAt(LocalDateTime.now());

        if (request.getCouponCodes() != null && !request.getCouponCodes().isEmpty()) {
            order.setCouponCodes(String.join(",", request.getCouponCodes()));
        }

        result.lineItems().forEach(l -> {
            CheckoutOrderLine line = new CheckoutOrderLine();
            line.setOrder(order);
            line.setSku(l.getSku());
            line.setQuantity(l.getQuantity());
            line.setUnitPrice(l.getUnitPrice());
            line.setSubtotal(l.getSubtotal());
            order.getLines().add(line);
        });

        result.appliedDiscounts().forEach(d -> {
            CheckoutOrderDiscount discount = new CheckoutOrderDiscount();
            discount.setOrder(order);
            discount.setCode(d.code());
            discount.setScope(d.scope());
            discount.setDescription(d.description());
            discount.setAmount(d.amount());
            order.getDiscounts().add(discount);
        });

        return checkoutOrderRepository.save(order);
    }

    private QuoteResponse mapToQuoteResponse(OrderCalculationResult result) {
        QuoteResponse response = new QuoteResponse();
        response.setCartId(result.cart().getId().toString());
        response.setCurrency("CLP");
        response.setLines(result.lineItems());
        response.setSubtotal(result.subtotal());
        response.setTotalDiscount(result.totalDiscount());
        response.setShippingCost(result.shippingCost());
        response.setTotal(result.finalTotal());
        response.setPickupAddress(result.pickupAddress());

        response.setDiscounts(mapDiscounts(result.appliedDiscounts()));

        return response;
    }

    private ConfirmResponse mapToConfirmResponse(CheckoutOrder order, OrderCalculationResult result) {
        ConfirmResponse response = new ConfirmResponse();
        response.setOrderId(order.getId().toString());
        response.setStatus("CONFIRMED");

        response.setCartId(result.cart().getId().toString());
        response.setCurrency("CLP");
        response.setSubtotal(result.subtotal());
        response.setTotalDiscount(result.totalDiscount());
        response.setShippingCost(result.shippingCost());
        response.setTotal(result.finalTotal());
        response.setPickupAddress(result.pickupAddress());
        response.setLines(result.lineItems());

        response.setDiscounts(mapDiscounts(result.appliedDiscounts()));

        return response;
    }

    /**
     * Maps the internal discount details to the response DTO.
     */
    private List<DiscountResponse> mapDiscounts(List<DiscountDetail> discounts) {
        return discounts.stream()
                .map(d -> {
                    DiscountResponse dr = new DiscountResponse();
                    dr.setCode(d.code());
                    dr.setScope(d.scope());
                    dr.setDescription(d.description());
                    dr.setAmount(d.amount());
                    return dr;
                })
                .toList();
    }

    /**
     * Maps the ShippingAddress entity to the ShippingAddressDto for API responses.
     */
    private ShippingAddressDto mapToAddressDto(ShippingAddress entity) {
        if (entity == null) {
            return null;
        }

        ShippingAddressDto dto = new ShippingAddressDto();
        dto.setStreet(entity.getStreet());
        dto.setCity(entity.getCity());
        dto.setZoneId(entity.getZoneId());
        return dto;
    }
}