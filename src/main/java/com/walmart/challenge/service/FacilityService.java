package com.walmart.challenge.service;

import com.walmart.challenge.dto.FacilityRequest;
import com.walmart.challenge.dto.FacilityResponse;
import com.walmart.challenge.dto.ShippingAddressDto;
import com.walmart.challenge.entity.Facility;
import com.walmart.challenge.entity.ShippingAddress;
import com.walmart.challenge.repository.FacilityRepository;
import com.walmart.challenge.repository.FacilityZoneDistanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service responsible for the lifecycle management of Facilities.
 * Handles creation, updates, retrieval, and safe deletion of facility entities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final FacilityZoneDistanceRepository facilityZoneDistanceRepository;

    /**
     * Retrieves all facilities configured in the system.
     *
     * @return List of facility details.
     */
    @Transactional(readOnly = true)
    public List<FacilityResponse> getAllFacilities() {
        return facilityRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Retrieves a specific facility by its unique identifier.
     *
     * @param id The UUID string of the facility.
     * @return The facility details.
     */
    @Transactional(readOnly = true)
    public FacilityResponse getFacility(String id) {
        UUID uuid = UUID.fromString(id);
        return facilityRepository.findById(uuid)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found: " + id));
    }

    /**
     * Creates a new facility. Enforces uniqueness on the facility name via database constraints.
     *
     * @param request The creation data.
     * @return The created facility details.
     */
    @Transactional
    public FacilityResponse createFacility(FacilityRequest request) {
        log.info("Creating new facility: {}", request.getName());

        Facility facility = new Facility();
        mapToEntity(facility, request);

        Facility savedFacility = facilityRepository.save(facility);
        return mapToResponse(savedFacility);
    }

    /**
     * Updates an existing facility.
     *
     * @param id      The UUID string of the facility to update.
     * @param request The updated data.
     * @return The updated facility details.
     */
    @Transactional
    public FacilityResponse updateFacility(String id, FacilityRequest request) {
        log.info("Updating facility: {}", id);

        UUID uuid = UUID.fromString(id);
        Facility facility = facilityRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found: " + id));

        mapToEntity(facility, request);

        Facility savedFacility = facilityRepository.save(facility);
        return mapToResponse(savedFacility);
    }

    /**
     * Deletes a facility and its associated zone distance records to maintain referential integrity.
     *
     * @param id The UUID string of the facility to delete.
     */
    @Transactional
    public void deleteFacility(String id) {
        log.info("Deleting facility: {}", id);

        UUID uuid = UUID.fromString(id);
        Facility facility = facilityRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found: " + id));

        // Cleanup associated distances before deleting the parent facility
        facilityZoneDistanceRepository.deleteByFacility(facility);

        facilityRepository.delete(facility);
    }

    /**
     * Maps the entity to the response DTO.
     */
    private FacilityResponse mapToResponse(Facility facility) {
        FacilityResponse response = new FacilityResponse();
        response.setId(facility.getId().toString());
        response.setName(facility.getName());
        response.setType(facility.getType());
        response.setPickupAvailable(facility.isPickupAvailable());

        if (facility.getLogisticAddress() != null) {
            ShippingAddressDto addrDto = new ShippingAddressDto();
            addrDto.setStreet(facility.getLogisticAddress().getStreet());
            addrDto.setCity(facility.getLogisticAddress().getCity());
            addrDto.setZoneId(facility.getLogisticAddress().getZoneId());
            response.setLogisticAddress(addrDto);
        }

        return response;
    }

    /**
     * Maps the request DTO to the entity.
     */
    private void mapToEntity(Facility facility, FacilityRequest request) {
        facility.setName(request.getName());
        facility.setType(request.getType());
        facility.setPickupAvailable(request.isPickupAvailable());

        if (request.getLogisticAddress() != null) {
            ShippingAddress addr = new ShippingAddress();
            addr.setStreet(request.getLogisticAddress().getStreet());
            addr.setCity(request.getLogisticAddress().getCity());
            addr.setZoneId(request.getLogisticAddress().getZoneId());
            facility.setLogisticAddress(addr);
        }
    }
}