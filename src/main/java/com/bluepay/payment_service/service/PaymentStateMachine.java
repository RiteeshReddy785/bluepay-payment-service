package com.bluepay.payment_service.service;



import org.springframework.stereotype.Component;

import com.bluepay.payment_service.data.PaymentStatus;

/**
 * Class: PaymentStateMachine
 *
 * Purpose:
 * Defines and validates the allowed status transitions for a payment.
 *
 * Example:
 * A payment with status PENDING can move to PROCESSING.
 * However, it cannot move directly from PENDING to SUCCEEDED.
 *
 * This class validates transitions but does not update the database.
 */
@Component
public class PaymentStateMachine {

    /**
     * Checks whether a payment can move from its current status
     * to the requested next status.
     *
     * @param current The current payment status.
     * @param next    The requested next payment status.
     * @return true if the transition is allowed; otherwise false.
     */
    public boolean canTransition(PaymentStatus current, PaymentStatus next) {

        // A transition is invalid if either status is missing.
        if (current == null || next == null) {
            return false;
        }

        // Define the allowed transitions for each current status.
        return switch (current) {

            // A newly created payment can start processing or be cancelled.
            case PENDING ->
                next == PaymentStatus.PROCESSING
                || next == PaymentStatus.CANCELLED;

            // A processing payment can succeed or fail.
            case PROCESSING ->
                next == PaymentStatus.SUCCEEDED
                || next == PaymentStatus.FAILED;

            // A failed payment may be retried by moving back to processing.
            case FAILED ->
                next == PaymentStatus.PROCESSING;

            // A successful payment may be refunded.
            case SUCCEEDED ->
                next == PaymentStatus.REFUNDED;

            // Cancelled and refunded payments are terminal in this model.
            case CANCELLED, REFUNDED -> false;
        };
    }

    /**
     * Validates a requested transition.
     * Throws an exception if the transition is not allowed.
     */
    public void validateTransition(PaymentStatus current, PaymentStatus next) {

        if (!canTransition(current, next)) {
            throw new IllegalStateException(
                "Invalid payment transition: " + current + " -> " + next
            );
        }
    }
}
