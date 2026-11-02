
package com.bluepay.payment_service.exception;

/**
 * Class: InvalidPaymentStateException
 *
 * Purpose:
 * Represents an attempt to move a payment from its current
 * status to a status that is not allowed by BluePay.
 *
 * Example:
 * A payment is PENDING, but a request tries to change it
 * directly to SUCCEEDED. This exception represents that error.
 *
 * The PaymentStateMachine defines which transitions are allowed.
 */
public class InvalidPaymentStateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidPaymentStateException(String message) {
        super(message);
    }
}
