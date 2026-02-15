package com.walmart.challenge.repository;

import com.walmart.challenge.entity.SizeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SizeCategoryRepository extends JpaRepository<SizeCategory, UUID> {
    /**
     * Returns all size categories ordered by their minimum volume
     * ascending. This method is used by the shipping service to
     * determine the appropriate category for a given total volume.
     */
    List<SizeCategory> findAllByOrderByMinVolumeAsc();
}