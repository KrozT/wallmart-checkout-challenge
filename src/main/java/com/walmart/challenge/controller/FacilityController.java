package com.walmart.challenge.controller;

import com.walmart.challenge.dto.FacilityRequest;
import com.walmart.challenge.dto.FacilityResponse;
import com.walmart.challenge.service.FacilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/v1/facilities")
@RequiredArgsConstructor
@Slf4j
public class FacilityController {

    private final FacilityService facilityService;

    /**
     * Retrieves all facilities.
     *
     * @return List of facilities.
     */
    @GetMapping
    public ResponseEntity<List<FacilityResponse>> getAll() {
        var response = facilityService.getAllFacilities();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a single facility by ID.
     *
     * @param id The unique identifier.
     * @return The facility details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FacilityResponse> getById(@PathVariable String id) {
        var response = facilityService.getFacility(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new facility.
     *
     * @param request The facility creation data.
     * @return The created facility with 201 Created status.
     */
    @PostMapping
    public ResponseEntity<FacilityResponse> create(@Valid @RequestBody FacilityRequest request) {
        log.info("REST request to create facility: {}", request.getName());

        var response = facilityService.createFacility(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    /**
     * Updates an existing facility.
     *
     * @param id      The unique identifier.
     * @param request The updated data.
     * @return The updated facility.
     */
    @PutMapping("/{id}")
    public ResponseEntity<FacilityResponse> update(@PathVariable String id,
                                                   @Valid @RequestBody FacilityRequest request) {
        log.info("REST request to update facility: {}", id);

        var response = facilityService.updateFacility(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a facility.
     *
     * @param id The unique identifier.
     * @return 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("REST request to delete facility: {}", id);

        facilityService.deleteFacility(id);
        return ResponseEntity.noContent().build();
    }
}