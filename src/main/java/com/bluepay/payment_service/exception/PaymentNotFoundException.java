package com.bluepay.payment_service.exception;



/**
 * Class: PaymentNotFoundException
 *
 * Purpose:
 * Represents the situation where a requested payment
 * does not exist in the BluePay database.
 *
 * Example:
 * A client requests payment ID 99999, but no payment
 * with that ID exists. The service throws this exception
 * with a meaningful message.
 *
 * A global exception handler will later convert this
 * exception into an HTTP 404 Not Found response.
 */
public class PaymentNotFoundException extends RuntimeException {

    // A serial version identifier for this exception class.
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception with a descriptive error message.
     *
     * @param message explains why the requested payment was not found
     */
    public PaymentNotFoundException(String message) {
        super(message);
    }
}
