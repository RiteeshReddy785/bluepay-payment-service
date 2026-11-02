package com.bluepay.payment_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bluepay.payment_service.data.*;

/**
 * Repository: PaymentStatusHistoryRepository
 *
 * Purpose:
 * Provides database operations for payment status history.
 *
 * Real-world example:
 * If payment ID 101 changes its status three times,
 * this repository can retrieve all three history records.
 *
 * Spring Data JPA automatically implements this interface.
 */
@Repository
public interface PaymentStatusHistoryRepository
        extends JpaRepository<PaymentStatusHistory, Long> {

    /**
     * Retrieves all status history records for a payment.
     *
     * Example:
     * findByPaymentId(101L)
     * returns all status changes associated with payment ID 101.
     */
    List<PaymentStatusHistory> findByPaymentId(Long paymentId);
}