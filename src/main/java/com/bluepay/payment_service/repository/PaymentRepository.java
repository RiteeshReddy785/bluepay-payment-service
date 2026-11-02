package com.bluepay.payment_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bluepay.payment_service.data.Payment;
import com.bluepay.payment_service.data.PaymentStatus;


public interface PaymentRepository  extends JpaRepository<Payment, Long>  {
	

    /**
     * Finds a payment using its unique payment reference.
     *
     * Example:
     * A client knows the payment reference UUID but not the
     * database ID. This method retrieves the matching payment.
     */
    java.util.Optional<Payment> findByPaymentReference(String paymentReference);

    List<Payment> findByCustomerId(String customerId);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    
    /**
     * Finds an existing payment using the customer's ID and idempotency key.
     *
     * Real-world example:
     * Customer CUST1001 submits a ₹1,000 payment with key "ORDER1001-RETRY-01".
     * If the customer retries the same request, this method finds the
     * original payment instead of requiring BluePay to create another one.
     *
     * @param customerId identifies the customer who submitted the payment
     * @param idempotencyKey identifies the logical payment request
     * @return the existing payment, or Optional.empty() if none exists
     */
    Optional<Payment> findByCustomerIdAndIdempotencyKey(
            String customerId,
            String idempotencyKey);

}
