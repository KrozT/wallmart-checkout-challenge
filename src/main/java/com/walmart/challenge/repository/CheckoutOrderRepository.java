package com.walmart.challenge.repository;

import com.walmart.challenge.entity.CheckoutOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckoutOrderRepository extends JpaRepository<CheckoutOrder, java.util.UUID> {
}