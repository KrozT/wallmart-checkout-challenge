package com.walmart.challenge.entity;

import com.walmart.challenge.enums.FacilityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "facility")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Facility {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    /** Name of the facility. Must be unique. */
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    /** Facility type (warehouse, distribution center or store). */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private FacilityType type;

    /**
     * Address of the facility used for logistical purposes such as
     * pickup.  This information may be exposed to the client when a
     * customer chooses to collect their order from this facility.
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "logistic_street")),
        @AttributeOverride(name = "city", column = @Column(name = "logistic_city")),
        @AttributeOverride(name = "zoneId", column = @Column(name = "logistic_zone"))
    })
    private ShippingAddress logisticAddress;

    /**
     * Indicates whether this facility supports customer pickup.  When true,
     * customers can elect to collect their orders from this facility
     * instead of having them delivered.
     */
    @Column(name = "pickup_available", nullable = false)
    private boolean pickupAvailable = false;
}