package com.bluepay.payment_service.controller;

import org.springframework.http.HttpStatus;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.bluepay.payment_service.data.PaymentStatus;
import com.bluepay.payment_service.data.PaymentStatusHistory;
import com.bluepay.payment_service.dto.CreatePaymentRequest;
import com.bluepay.payment_service.dto.PaymentResponse;
import com.bluepay.payment_service.dto.PaymentStatusHistoryResponse;
import com.bluepay.payment_service.repository.PaymentStatusHistoryRepository;
import com.bluepay.payment_service.service.PaymentService;
import com.bluepay.payment_service.service.PaymentStatusHistoryService;

/**
 * Class: PaymentController
 *
 * Purpose:
 * Exposes HTTP endpoints through which clients interact with BluePay.
 *
 * Example:
 * A client sends POST /api/payments with payment details in JSON.
 * This controller validates the request and delegates the work
 * to PaymentService, then returns the payment response.
 *
 * The controller does not access the repository directly.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentStatusHistoryService historyService;


    // Spring injects PaymentService through this constructor.
    public PaymentController(PaymentService paymentService,PaymentStatusHistoryService historyService) {
        this.paymentService = paymentService;
		this.historyService = historyService;
    }

    /**
     * Creates a new payment.
     *
     * @Valid triggers the validation rules defined in the request DTO.
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {

        // Delegate business logic to the service layer.
        PaymentResponse response = paymentService.createPayment(request);

        // Return HTTP 201 Created along with the payment response.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    

    /**
     * Retrieves a payment by its database ID.
     *
     * Example:
     * GET /api/payments/1
     * Returns the payment details if payment ID 1 exists.
     */
    @GetMapping("jaga/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long id) {

        // Delegate retrieval and not-found handling to the service.
        PaymentResponse response = paymentService.getPaymentById(id);

        // Return HTTP 200 OK with the payment details.
        return ResponseEntity.ok(response);
    }


    /**
     * Retrieves a payment by its unique payment reference.
     *
     * Example:
     * GET /api/payments/reference/{reference}
     *
     * Returns the matching payment if it exists.
     */
    @GetMapping("/reference/{reference}")
    public ResponseEntity<PaymentResponse> getPaymentByReference(
            @PathVariable String reference) {

        // Delegate lookup and not-found handling to the service.
        PaymentResponse response =
                paymentService.getPaymentByReference(reference);

        // Return HTTP 200 OK with the payment details.
        return ResponseEntity.ok(response);
    }
    

    /**
     * Retrieves payments with pagination and sorting.
     *
     * Example:
     * GET /api/payments?page=0&size=10&sortBy=createdAt&direction=desc
     *
     * Returns the first 10 payments, newest first.
     */
    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        // Delegate pagination, sorting and retrieval to the service.
        Page<PaymentResponse> response = paymentService.getPayments(
                page, size, sortBy, direction);

        // Return HTTP 200 OK with the requested page.
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByCustomerId(
            @PathVariable String customerId) {

        List<PaymentResponse> payments =
                paymentService.getPaymentsByCustomerId(customerId);

        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(
            @PathVariable PaymentStatus status) {

        List<PaymentResponse> payments =
                paymentService.getPaymentsByStatus(status);

        return ResponseEntity.ok(payments);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable Long id) {

        // Delegate the cancellation rules and database update to the service.
        PaymentResponse response = paymentService.cancelPayment(id);

        // Return HTTP 200 OK along with the updated payment response.
        return ResponseEntity.ok(response);
    }
    
//    @GetMapping("/{paymentId}/history")
//    public ResponseEntity<List<PaymentStatusHistory>> getPaymentHistory(
//            @PathVariable Long paymentId) {
//
//        // Ask the service to retrieve all history records for this payment.
//        List<PaymentStatusHistory> history =
//                historyService.getPaymentHistory(paymentId);
//
//        // Return HTTP 200 OK with the history records.
//        return ResponseEntity.ok(history);
//    }
//    

/**
 * Retrieves payment history for a specific payment.
 *
 * Example:
 * GET /api/payments/101/history
 *
 * Returns the payment's complete status history as DTOs,
 * without exposing the database entity directly.
 *
 * @param paymentId database ID of the payment
 * @return list of payment history response objects
 */

    /**
     * Retrieves payment history for a particular payment.
     *
     * @param paymentId database ID of the payment
     * @return list of payment history DTOs
     */
    @GetMapping("/{paymentId}/history")
    public ResponseEntity<List<PaymentStatusHistoryResponse>> getPaymentHistory1(
            @PathVariable Long paymentId) {

        // Step 1: Retrieve payment history from the service.
        List<PaymentStatusHistoryResponse> history =
                historyService.getPaymentHistory(paymentId);

        // Step 2: Return HTTP 200 OK with the DTO response.
        return ResponseEntity.ok(history);
    }

}
