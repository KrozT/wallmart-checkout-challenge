package com.walmart.challenge.repository;

import com.walmart.challenge.entity.Facility;
import com.walmart.challenge.entity.FacilityZoneDistance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacilityZoneDistanceRepository extends JpaRepository<FacilityZoneDistance, UUID> {

    /**
     * Retrieves the single nearest facility distance for a given zone.
     * This method leverages database sorting and limiting to avoid fetching
     * all records into memory.
     *
     * @param zoneId the identifier of the shipping zone
     * @return an optional containing the shortest distance record found
     */
    Optional<FacilityZoneDistance> findTopByZoneIdOrderByDistanceAsc(String zoneId);

    /**
     * Deletes all distance records associated with a specific facility.
     * This is used during the facility deletion process to enforce referential integrity
     * and clean up orphaned records.
     *
     * @param facility the facility entity to remove distances for
     */
    void deleteByFacility(Facility facility);
}