package com.bluepay.payment_service.service;




import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bluepay.payment_service.data.Payment;
import com.bluepay.payment_service.data.PaymentStatusHistory;
import com.bluepay.payment_service.dto.PaymentStatusHistoryResponse;
import com.bluepay.payment_service.dto.mapper.PaymentStatusHistoryMapper;
import com.bluepay.payment_service.exception.PaymentNotFoundException;
import com.bluepay.payment_service.repository.PaymentRepository;
import com.bluepay.payment_service.repository.PaymentStatusHistoryRepository;

/**
 * Service: PaymentStatusHistoryService
 *
 * Purpose:
 * Records and retrieves the status history of payments.
 *
 * Real-world example:
 * Payment 101 changes from PENDING to PROCESSING.
 * This service records that transition with its timestamp
 * and reason in the payment_status_history table.
 */
@Service
public class PaymentStatusHistoryService {

    // Repository used to communicate with the history table.
    private final PaymentStatusHistoryRepository historyRepository;
    private final PaymentStatusHistoryMapper historyMapper;
    @Autowired
    private  PaymentRepository paymentRepository;

    /**
     * Constructor injection.
     *
     * Spring automatically provides the repository implementation.
     */
    public PaymentStatusHistoryService(
            PaymentStatusHistoryRepository historyRepository,PaymentStatusHistoryMapper historyMapper) {
        this.historyRepository = historyRepository;
		this.historyMapper =  historyMapper ;
    }

    /**
     * Records a payment status transition.
     *
     * @param payment the payment whose status changed
     * @param previousStatus status before the change
     * @param newStatus status after the change
     * @param reason explanation for the transition
     */
    @Transactional
    public void recordStatusChange(
            Payment payment,
            String previousStatus,
            String newStatus,
            String reason) {

        // Create a new history entity for this transition.
        PaymentStatusHistory history = new PaymentStatusHistory();

        // Associate the history record with its payment.
        history.setPayment(payment);

        // Store the status before the transition.
        history.setPreviousStatus(previousStatus);

        // Store the status after the transition.
        history.setNewStatus(newStatus);

        // Record the current date and time in UTC.
        history.setChangedAt(Instant.now());

        // Store why the transition happened.
        history.setReason(reason);

        // Insert the history record into MySQL.
        historyRepository.save(history);
    }

    /**
     * Retrieves all history records for a payment.
     *
     * Example:
     * getPaymentHistory(101L) returns all status changes
     * associated with payment ID 101.
     *
     * @param paymentId database ID of the payment
     * @return list of payment history records
     */
    
    public List<PaymentStatusHistoryResponse> getPaymentHistory(Long paymentId) {

        // Step 1: Check whether the payment exists in the payments table.
        // If it does not exist, throw PaymentNotFoundException (HTTP 404).
        paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found with ID: " + paymentId));

        // Step 2: Fetch all status history records for this payment.
        List<PaymentStatusHistory> historyRecords =
                historyRepository.findByPaymentId(paymentId);

        // Step 3: Convert entity objects into response DTO objects.
        return historyRecords.stream()
                .map(historyMapper::toResponse)
                .toList();
    }
}
