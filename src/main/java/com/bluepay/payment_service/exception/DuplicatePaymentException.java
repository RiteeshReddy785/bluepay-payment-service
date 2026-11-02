
package com.bluepay.payment_service.exception;

/**
 * Class: DuplicatePaymentException
 *
 * Purpose:
 * Represents a payment request that has already been processed
 * or submitted with the same idempotency key.
 *
 * Example:
 * A client submits a payment request with idempotency key
 * "order-1234567890abcd" and retries the same request.
 * BluePay can use this exception when the request is identified
 * as a duplicate, according to our idempotency rules.
 *
 * This class represents the error only. The service will
 * implement the actual duplicate detection.
 */
public class DuplicatePaymentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicatePaymentException(String message) {
        super(message);
    }
}
