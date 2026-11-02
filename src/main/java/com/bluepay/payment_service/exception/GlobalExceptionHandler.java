package com.bluepay.payment_service.exception;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Class: GlobalExceptionHandler
 *
 * Purpose:
 * Handles application exceptions centrally and converts them
 * into meaningful HTTP error responses.
 *
 * Example:
 * If a client requests a payment that does not exist,
 * PaymentNotFoundException is caught here and returned
 * as HTTP 404 Not Found with a useful error message.
 *
 * This avoids duplicating exception-handling logic
 * across multiple controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles requests for payments that do not exist.
     */
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(
            PaymentNotFoundException exception) {

        // Create a structured error response for the client.
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "Payment Not Found",
                exception.getMessage()
        );

        // Return HTTP 404 along with the error details.
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    

    /**
     * Handles duplicate payment requests.
     *
     * Example:
     * If a payment request is submitted again with an idempotency
     * key that has already been used, return HTTP 409 Conflict.
     */
    @ExceptionHandler(DuplicatePaymentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatePayment(
            DuplicatePaymentException exception) {

        // Build a structured error response.
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Duplicate Payment",
                exception.getMessage()
        );

        // Return HTTP 409 Conflict.
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }


    /**
     * Handles invalid payment status transitions.
     *
     * Example:
     * A payment is PENDING, but an operation attempts to
     * change it directly to SUCCEEDED.
     */
    @ExceptionHandler(InvalidPaymentStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentState(
            InvalidPaymentStateException exception) {

        // Build a structured error response.
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Invalid Payment State",
                exception.getMessage()
        );

        // Return HTTP 409 Conflict.
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    

    /**
     * Handles validation failures in incoming request DTOs.
     *
     * Example:
     * If a client sends amount = -500 or leaves currency empty,
     * this handler returns HTTP 400 with the validation messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException exception) {

        // Collect all field validation errors into one readable message.
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));

        // Build a structured error response.
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                message
        );

        // Return HTTP 400 Bad Request.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    

    /**
     * Handles unexpected exceptions that were not handled elsewhere.
     *
     * Example:
     * An unexpected application error occurs while processing
     * a request. The client receives a generic HTTP 500 response
     * without internal implementation details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception) {

        // Log the exception on the server for troubleshooting.
        // We will add a proper logging framework configuration later.
        System.err.println(
                "Unexpected application error: " + exception.getMessage()
        );

        // Do not expose internal exception details to the client.
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later."
        );

        // Return HTTP 500 Internal Server Error.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }


    /**
     * Represents the error information returned to the client.
     */
    public record ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message) {
    }
}
