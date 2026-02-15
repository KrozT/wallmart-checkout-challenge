package com.walmart.challenge.entity;

import com.walmart.challenge.enums.CouponType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "coupon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    /** Coupon code used by clients.  Must be unique. */
    @Column(unique = true, nullable = false)
    private String code;

    /** Human-readable description of the coupon. */
    @Column
    private String description;

    /** The type of coupon (ORDER or SHIPPING). */
    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_type", nullable = false)
    private CouponType couponType;

    /** Percentage discount applied if non-null (e.g. 0.10 for 10%). */
    @Column(precision = 5, scale = 4)
    private BigDecimal percentage;

    /** Fixed amount discount applied if non-null (e.g. 5000 for 5k CLP). */
    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    /** Indicates whether the coupon is active.  Inactive coupons are ignored. */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Determines whether this coupon may be combined with other coupons.  When
     * {@code false} the coupon is considered exclusive and should not be
     * stacked with other coupons of a different type.  In the current
     * implementation only one coupon per type is applied regardless of this
     * flag, but the property allows future extension where multiple coupon
     * types could be combined as long as at most one non‑stackable coupon is
     * present.  For example, order coupons such as {@code 10DEC} and
     * {@code 20DEC} are marked non‑stackable so that they cannot be used
     * together, while a shipping coupon like {@code FREE_SHIPPING} is
     * stackable and may be combined with an order coupon.
     */
    @Column(name = "stackable", nullable = false)
    private boolean stackable = true;

    /**
     * Remaining uses of this coupon.  When this value is {@code null}
     * the coupon may be used an unlimited number of times; otherwise
     * each successful application of the coupon decrements this
     * counter and the coupon is no longer considered valid once it
     * reaches zero.  This mechanism allows for limited‑use coupons.
     */
    @Column(name = "remaining_uses")
    private Integer remainingUses;

    /**
     * Expiration timestamp for this coupon.  If {@code null} the
     * coupon does not expire.  Coupons with an expiration before the
     * current date/time are considered invalid and will not be
     * applied.  Time is stored in the database using the default
     * timezone of the JVM; consider using UTC in a real system.
     */
    @Column(name = "expiry")
    private LocalDateTime expiry;
}