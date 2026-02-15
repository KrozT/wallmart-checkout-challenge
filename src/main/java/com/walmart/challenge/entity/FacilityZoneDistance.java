package com.walmart.challenge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "facility_zone_distance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacilityZoneDistance {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    /**
     * Identifier of the zone this distance applies to. Must match
     * the zoneId stored in {@link ShippingAddress}.
     */
    @Column(name = "zone_id", nullable = false)
    private String zoneId;

    /**
     * Distance in kilometres between the facility and the given zone.
     */
    @Column(name = "distance", precision = 19, scale = 4, nullable = false)
    private BigDecimal distance;
}