package com.walmart.challenge.bootstrap;

import com.walmart.challenge.entity.*;
import com.walmart.challenge.enums.*;
import com.walmart.challenge.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Seeds the database with a baseline set of data specifically for
 * development and testing environments.
 * <p>
 * This component acts as a "Seeder" to populate the database with
 * both reference data (categories, rates) and sample transactional data
 * (products, facilities). It is strictly profiled for non-production use
 * to prevent data pollution in live environments.
 * </p>
 */
@Component
@Profile("!prod")
@RequiredArgsConstructor
@Slf4j
public class DevelopmentDataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;
    private final PaymentDiscountRepository paymentDiscountRepository;
    private final SizeCategoryRepository sizeCategoryRepository;
    private final ProductDimensionRepository productDimensionRepository;
    private final ShippingRateRepository shippingRateRepository;
    private final FacilityRepository facilityRepository;
    private final FacilityZoneDistanceRepository facilityZoneDistanceRepository;
    private final CouponRepository couponRepository;
    private final CartRepository cartRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting development data seeding...");

        seedProductsAndDimensions();
        seedSizeCategories();
        seedFacilitiesAndLogistics();
        seedShippingRates();
        seedPromotions();
        seedPaymentDiscounts();
        seedCoupons();
        seedDemoCarts();

        log.info("Development data seeding completed successfully.");
    }

    /**
     * Seeds catalog products and their physical dimensions.
     */
    private void seedProductsAndDimensions() {
        if (productRepository.count() > 0) return;

        createProductWithDimensions("p-001", new BigDecimal("10000"), new BigDecimal("10"), new BigDecimal("5"), new BigDecimal("2"));
        createProductWithDimensions("p-010", new BigDecimal("5000"), new BigDecimal("15"), new BigDecimal("20"), new BigDecimal("3"));
        createProductWithDimensions("p-003", new BigDecimal("20000"), new BigDecimal("25"), new BigDecimal("10"), new BigDecimal("4"));
    }

    private void createProductWithDimensions(String sku, BigDecimal price, BigDecimal h, BigDecimal w, BigDecimal d) {
        var product = new Product();
        product.setSku(sku);
        product.setUnitPrice(price);
        productRepository.save(product);

        var dim = new ProductDimension(null, product, h, w, d);
        productDimensionRepository.save(dim);
    }

    /**
     * Seeds volumetric size categories used for shipping calculation.
     */
    private void seedSizeCategories() {
        if (sizeCategoryRepository.count() > 0) return;

        createSizeCategory("XS", BigDecimal.ZERO, new BigDecimal("1000"));
        createSizeCategory("S", new BigDecimal("1001"), new BigDecimal("10000"));
        createSizeCategory("M", new BigDecimal("10001"), new BigDecimal("50000"));
        createSizeCategory("L", new BigDecimal("50001"), new BigDecimal("100000"));
        createSizeCategory("XL", new BigDecimal("100001"), null);
    }

    private void createSizeCategory(String name, BigDecimal min, BigDecimal max) {
        sizeCategoryRepository.save(new SizeCategory(null, name, min, max));
    }

    /**
     * Seeds facilities and their distance to specific shipping zones.
     */
    private void seedFacilitiesAndLogistics() {
        if (facilityRepository.count() > 0) return;

        var warehouse = createFacility("WarehouseNorth", FacilityType.WAREHOUSE, "zone-1", false);
        var dc = createFacility("DistributionCenterWest", FacilityType.DISTRIBUTION_CENTER, "zone-2", true);
        var store = createFacility("StoreCentral", FacilityType.STORE, "zone-3", true);

        createDistances(warehouse, 10, 20, 30);
        createDistances(dc, 15, 12, 25);
        createDistances(store, 5, 30, 40);
    }

    private Facility createFacility(String name, FacilityType type, String zoneId, boolean pickup) {
        var f = new Facility();
        f.setName(name);
        f.setType(type);
        f.setLogisticAddress(new ShippingAddress(name + " Address", "City", zoneId));
        f.setPickupAvailable(pickup);
        return facilityRepository.save(f);
    }

    private void createDistances(Facility f, int z1, int z2, int z3) {
        facilityZoneDistanceRepository.save(new FacilityZoneDistance(null, f, "zone-1", BigDecimal.valueOf(z1)));
        facilityZoneDistanceRepository.save(new FacilityZoneDistance(null, f, "zone-2", BigDecimal.valueOf(z2)));
        facilityZoneDistanceRepository.save(new FacilityZoneDistance(null, f, "zone-3", BigDecimal.valueOf(z3)));
    }

    /**
     * Seeds shipping rates associated with size categories.
     */
    private void seedShippingRates() {
        if (shippingRateRepository.count() > 0) return;

        var categories = sizeCategoryRepository.findAll();
        createRate(categories, "XS", 1000, 50);
        createRate(categories, "S", 2000, 100);
        createRate(categories, "M", 3000, 150);
        createRate(categories, "L", 4000, 200);
        createRate(categories, "XL", 5000, 250);
    }

    private void createRate(java.util.List<SizeCategory> cats, String name, int base, int perKm) {
        cats.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .ifPresent(c -> shippingRateRepository.save(new ShippingRate(null, c, BigDecimal.valueOf(base), BigDecimal.valueOf(perKm))));
    }

    /**
     * Seeds the rule engine promotions.
     */
    private void seedPromotions() {
        if (promotionRepository.count() > 0) return;

        var orderPromo = createBasePromo("PROMO_10", "10% off order", 1);
        addRule(orderPromo, RuleType.ACTION, "PercentageDiscountAction", "percentage", "0.10");
        promotionRepository.save(orderPromo);

        productRepository.findBySku("p-010").ifPresent(p -> {
            var skuPromo = createBasePromo("PROMO_SKU_p-010", "50% off SKU p-010", 2);

            var condition = createRule(skuPromo, RuleType.CONDITION, "SkuMatchCondition");
            addParam(condition, "productId", p);
            skuPromo.getRules().add(condition);

            var action = createRule(skuPromo, RuleType.ACTION, "SkuPercentageDiscountAction");
            addParam(action, "percentage", "0.50");
            addParam(action, "productId", p);
            skuPromo.getRules().add(action);

            promotionRepository.save(skuPromo);
        });

        var fixedPromo = createBasePromo("PROMO_5000_OFF", "CLP 5000 off orders 50k+", 3);
        addRule(fixedPromo, RuleType.CONDITION, "MinCartTotalCondition", "minTotal", "50000");
        addRule(fixedPromo, RuleType.ACTION, "FixedAmountDiscountAction", "amount", "5000");
        promotionRepository.save(fixedPromo);
    }

    private Promotion createBasePromo(String code, String name, int priority) {
        var p = new Promotion();
        p.setCode(code);
        p.setName(name);
        p.setDescription(name);
        p.setPriority(priority);
        p.setActive(true);
        return p;
    }

    private void addRule(Promotion p, RuleType type, String implKey, String paramKey, String paramVal) {
        var rule = createRule(p, type, implKey);
        addParam(rule, paramKey, paramVal);
        p.getRules().add(rule);
    }

    private PromotionRule createRule(Promotion p, RuleType type, String implKey) {
        var r = new PromotionRule();
        r.setPromotion(p);
        r.setRuleType(type);
        r.setImplementationKey(implKey);
        return r;
    }

    private void addParam(PromotionRule rule, String key, Object val) {
        var param = new RuleParameter();
        param.setRule(rule);
        param.setParamKey(key);
        if (val instanceof String s) param.setNumericValue(new BigDecimal(s));
        if (val instanceof Product p) param.setRelatedProduct(p);
        rule.getParameters().add(param);
    }

    /**
     * Seeds static discounts associated with payment methods.
     */
    private void seedPaymentDiscounts() {
        if (paymentDiscountRepository.count() > 0) return;

        var pd1 = new PaymentDiscount();
        pd1.setPaymentMethod(PaymentMethod.DEBIT);
        pd1.setPercentage(new BigDecimal("0.10"));
        pd1.setDescription("10% discount for debit");
        paymentDiscountRepository.save(pd1);

        var pd2 = new PaymentDiscount();
        pd2.setPaymentMethod(PaymentMethod.CREDIT);
        pd2.setAmount(BigDecimal.valueOf(2000));
        pd2.setDescription("2000 CLP discount for credit");
        paymentDiscountRepository.save(pd2);
    }

    /**
     * Seeds predefined coupons.
     */
    private void seedCoupons() {
        if (couponRepository.count() > 0) return;

        createCoupon("10DESC", CouponType.ORDER, "0.10", null, false);
        createCoupon("20DESC", CouponType.ORDER, "0.20", null, false);
        createCoupon("FREE_SHIPPING", CouponType.SHIPPING, null, null, true);
    }

    private void createCoupon(String code, CouponType type, String pct, String amt, boolean stackable) {
        var c = new Coupon();
        c.setCode(code);
        c.setCouponType(type);
        if (pct != null) c.setPercentage(new BigDecimal(pct));
        if (amt != null) c.setAmount(new BigDecimal(amt));
        c.setStackable(stackable);
        c.setActive(true);
        couponRepository.save(c);
    }

    /**
     * Seeds demonstration carts configured to trigger specific business rules.
     * Logs the generated UUIDs for easy testing via Swagger/Postman.
     */
    private void seedDemoCarts() {
        if (cartRepository.count() > 0) return;
        log.info("Seeding Demo Carts...");

        // 1. Promo Cart: Triggers "50% off SKU p-010"
        createCart("Demo Promo SKU", "zone-1", Map.of(
                "p-010", 2 // 2 units of the promo item
        ));

        // 2. High Value Cart: Triggers "5000 OFF for orders > 50k"
        createCart("Demo High Value", "zone-2", Map.of(
                "p-003", 3 // 3 * 20000 = 60000 (Triggers > 50k rule)
        ));

        // 3. Mixed Cart: Triggers complex shipping
        createCart("Demo Mixed Items", "zone-3", Map.of(
                "p-001", 1,
                "p-010", 1,
                "p-003", 1
        ));
    }

    private void createCart(String description, String zoneId, Map<String, Integer> items) {
        Cart cart = new Cart();

        ShippingAddress addr = new ShippingAddress();
        addr.setStreet("Demo Street 123");
        addr.setCity("Demo City");
        addr.setZoneId(zoneId);
        cart.setShippingAddress(addr);

        items.forEach((sku, qty) -> {
            productRepository.findBySku(sku).ifPresent(p -> {
                CartItem ci = new CartItem();
                ci.setProduct(p);
                ci.setQuantity(qty);
                ci.setCart(cart);
                cart.getItems().add(ci);
            });
        });

        Cart saved = cartRepository.save(cart);
        log.info("Created Cart '{}' (Zone: {}): {}", description, zoneId, saved.getId());
    }
}