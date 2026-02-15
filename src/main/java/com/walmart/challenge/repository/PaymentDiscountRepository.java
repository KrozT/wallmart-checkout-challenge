package com.walmart.challenge.repository;

import com.walmart.challenge.entity.PaymentDiscount;
import com.walmart.challenge.enums.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentDiscountRepository extends JpaRepository<PaymentDiscount, java.util.UUID> {
    Optional<PaymentDiscount> findByPaymentMethod(PaymentMethod paymentMethod);
}