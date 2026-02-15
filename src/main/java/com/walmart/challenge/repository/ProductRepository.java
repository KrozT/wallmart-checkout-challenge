package com.walmart.challenge.repository;

import com.walmart.challenge.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, java.util.UUID> {
    Optional<Product> findBySku(String sku);
}