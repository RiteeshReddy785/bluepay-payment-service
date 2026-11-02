package com.bluepay.payment_service.service;



import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bluepay.payment_service.dto.CreatePaymentRequest;
import com.bluepay.payment_service.dto.PaymentResponse;
import com.bluepay.payment_service.dto.PaymentStatusHistoryResponse;
import com.bluepay.payment_service.data.Payment;
import com.bluepay.payment_service.data.PaymentStatus;
import com.bluepay.payment_service.data.PaymentStatusHistory;
import com.bluepay.payment_service.dto.mapper.PaymentMapper;
import com.bluepay.payment_service.dto.mapper.PaymentStatusHistoryMapper;
import com.bluepay.payment_service.exception.DuplicatePaymentException;
import com.bluepay.payment_service.exception.PaymentNotFoundException;
import com.bluepay.payment_service.repository.PaymentRepository;
import com.bluepay.payment_service.repository.PaymentStatusHistoryRepository;

/**
 * Class: PaymentService
 *
 * Purpose:
 * Coordinates the business logic for payment operations.
 *
 * Example:
 * When a client requests a payment of INR 500, this service
 * converts the request into a Payment entity, saves it through
 * the repository, and returns a PaymentResponse.
 *
 * The controller calls this service instead of accessing
 * the repository directly.
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentStateMachine paymentStateMachine;
    @Autowired
    private  PaymentStatusHistoryMapper historyMapper;
 // Service responsible for recording payment status history.
    private final PaymentStatusHistoryService paymentStatusHistoryService;
    private final PaymentStatusHistoryRepository historyRepository;

    // Constructor injection lets Spring provide the required components.
    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentMapper paymentMapper,
            PaymentStateMachine paymentStateMachine,PaymentStatusHistoryService paymentStatusHistoryService,
            PaymentStatusHistoryRepository historyRepository) {

        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.paymentStateMachine = paymentStateMachine;
		this.paymentStatusHistoryService = paymentStatusHistoryService;
		this.historyRepository = historyRepository;
    }

    

    /**
     * Retrieves a payment using its database ID.
     *
     * Example:
     * If payment ID 101 exists, return its details.
     * If it does not exist, throw PaymentNotFoundException.
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {

        // Search for the payment using the repository.
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found with ID: " + id
                        )
                );

        // Convert the entity into a response DTO.
        return paymentMapper.toResponse(payment);
    }


    /**
     * Retrieves a payment using its unique payment reference.
     *
     * Example:
     * A client supplies a payment reference UUID instead of
     * the database ID. This method returns the matching payment.
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReference(String paymentReference) {

        // Search for a payment with the supplied reference.
        Payment payment = paymentRepository
                .findByPaymentReference(paymentReference)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found with reference: " + paymentReference
                        )
                );

        // Convert the database entity into the API response DTO.
        return paymentMapper.toResponse(payment);
    }
    

    /**
     * Retrieves payments in pages with configurable sorting.
     *
     * Example:
     * page = 0, size = 10, sortBy = createdAt, direction = desc
     * returns the first 10 payments, newest first.
     */
    
    //GET http://localhost:8080/api/payments?page=0&size=10&sortBy=createdAt&direction=desc
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPayments(
            int page,
            int size,
            String sortBy,
            String direction) {
    	

        // Reject invalid pagination values.
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }

        // Allow only known entity fields to be used for sorting.
        if (!java.util.Set.of("id", "amount", "createdAt", "updatedAt", "status")
                .contains(sortBy)) {
            throw new IllegalArgumentException("Unsupported sort field: " + sortBy);
        }

        // Convert the direction string into Spring's Sort direction.
        Sort.Direction sortDirection;
        try {
        	sortDirection =
        	        Sort.Direction.fromString(direction.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Sort direction must be 'asc' or 'desc'");
        }

        // Build the pagination and sorting request.
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        // Fetch only the requested page from the database.
        Page<Payment> payments = paymentRepository.findAll(pageable);

        // Convert each entity into a response DTO.
        return payments.map(paymentMapper::toResponse);
    }
    
    
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByCustomerId(String customerId) {

        List<Payment> payments = paymentRepository.findByCustomerId(customerId);

        return payments.stream()
                .map(paymentMapper::toResponse)
                .toList();
    }
    
    /**
     * Retrieves all payments matching the requested status.
     *
     * Example:
     * getPaymentsByStatus(PaymentStatus.SUCCEEDED)
     * returns payment responses for successful payments.
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {

        List<Payment> payments = paymentRepository.findByStatus(status);

        return payments.stream()
                .map(paymentMapper::toResponse)
                .toList();
    }
    

/**
 * Cancels an existing payment when its current status allows cancellation.
 *
 * Real-world example:
 * A customer creates a payment for INR 500, but it is still PENDING.
 * This method changes its status to CANCELLED.
 *
 * Important:
 * This method updates the database status only. It does not initiate a
 * refund, reverse a bank transfer, or cancel an external payment-provider
 * transaction.
 *
 * @param id the database ID of the payment to cancel
 * @return the updated payment details as a PaymentResponse DTO
 * @throws PaymentNotFoundException if no payment exists with this ID
 * @throws InvalidPaymentStateException if cancellation is not allowed
 */
//@Transactional
//public PaymentResponse cancelPayment(Long id) {
//
//    // Step 1: Find the payment in the database using its primary key.
//    // If it does not exist, stop and report a payment-not-found error.
//    Payment payment = paymentRepository.findById(id)
//            .orElseThrow(() -> new PaymentNotFoundException(
//                    "Payment not found with ID: " + id));
//
//    // Step 2: Ask the state machine whether the current status can
//    // transition to CANCELLED. For example, PENDING -> CANCELLED is allowed.
//    paymentStateMachine.validateTransition(
//            payment.getStatus(),
//            PaymentStatus.CANCELLED);
//
//    // Step 3: Update the payment's status in the Java entity.
//    payment.setStatus(PaymentStatus.CANCELLED);
//
//    // Step 4: Save the updated payment through Spring Data JPA.
//    // Because this method is transactional, the database change is
//    // committed when the transaction completes successfully.
//    Payment updatedPayment = paymentRepository.save(payment);
//
//    // Step 5: Convert the entity into a response DTO so the API returns
//    // the intended payment fields instead of exposing the entity directly.
//    return paymentMapper.toResponse(updatedPayment);
//}


/**
 * Cancels a payment and records the cancellation in payment history.
 *
 * Example:
 * Payment ID 101 changes from PENDING to CANCELLED.
 *
 * The payment update and history record are saved in one transaction.
 *
 * @param id database ID of the payment
 * @return updated payment details
 */
@Transactional
public PaymentResponse cancelPayment(Long id) {

    // Step 1: Find the payment in the database.
    Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new PaymentNotFoundException(
                    "Payment not found with ID: " + id));

    // Step 2: Capture the current status before changing it.
    PaymentStatus previousStatus = payment.getStatus();

    // Step 3: Verify that cancellation is allowed.
    // Example: PENDING -> CANCELLED is allowed.
    paymentStateMachine.validateTransition(
            previousStatus,
            PaymentStatus.CANCELLED);

    // Step 4: Change the payment status to CANCELLED.
    payment.setStatus(PaymentStatus.CANCELLED);

    // Step 5: Save the updated payment.
    Payment updatedPayment = paymentRepository.save(payment);

    // Step 6: Record the cancellation in payment history.
    paymentStatusHistoryService.recordStatusChange(
            updatedPayment,
            previousStatus.name(),
            PaymentStatus.CANCELLED.name(),
            "Payment cancelled by request");

    // Step 7: Return the updated payment details.
    return paymentMapper.toResponse(updatedPayment);
}



/**
 * Updates a payment's status only when the state machine
 * permits the requested transition.
 *
 * Example:
 * Payment ID 15 is PENDING.
 * Updating it to PROCESSING is allowed.
 * Updating it directly to SUCCEEDED is rejected.
 *
 * @param id database ID of the payment
 * @param newStatus requested status
 * @return updated payment details
 * @throws PaymentNotFoundException if the payment does not exist
 * @throws InvalidPaymentStateException if the transition is invalid
 */

/**
 * Updates a payment status and records the transition in payment history.
 *
 * Example:
 * Payment ID 101 changes from PENDING to PROCESSING.
 * The new status is saved in payments, and the transition
 * is recorded in payment_status_history.
 */
@Transactional
public PaymentResponse updatePaymentStatus(Long id, PaymentStatus newStatus) {

    // 1. Find the payment using its database ID.
    Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new PaymentNotFoundException(
                    "Payment not found with ID: " + id));

    // 2. Store the old status before changing it.
    // Example: previousStatus = PENDING.
    PaymentStatus previousStatus = payment.getStatus();

    // 3. Check whether this status transition is allowed.
    // Example: PENDING -> PROCESSING is allowed.
    paymentStateMachine.validateTransition(previousStatus, newStatus);

    // 4. Update the payment's current status.
    payment.setStatus(newStatus);

    // 5. Save the updated payment in the payments table.
    Payment updatedPayment = paymentRepository.save(payment);

    // 6. Record the old status, new status, timestamp and reason
    // in the payment_status_history table.
    paymentStatusHistoryService.recordStatusChange(
            updatedPayment,
            previousStatus.name(),
            newStatus.name(),
            "Payment status changed from " + previousStatus + " to " + newStatus
    );

    // 7. Return the updated payment as a response DTO.
    return paymentMapper.toResponse(updatedPayment);
}




    /**
     * Creates a payment and saves it to the database.
     */

/**
 * Creates a payment while protecting against duplicate requests.
 *
 * Example:
 * If CUST1001 submits the same request twice with the same
 * idempotency key, the second request returns the existing payment.
 *
 * @param request validated details for the payment
 * @return the existing payment or the newly created payment
 */

/**
 * Creates a payment safely using an idempotency key.
 *
 * Example:
 * First request:  CUST1001, INR 1000, key ORDER1001-RETRY-01
 * Retry request:  CUST1001, INR 1000, same key
 * Result: returns the existing payment.
 *
 * If the same key is reused with a different amount or other
 * payment details, the request is rejected.
 */

@Transactional
public PaymentResponse createPayment(CreatePaymentRequest request) {

    // STEP 1: Check whether this customer has already used
    // the supplied idempotency key.
    Optional<Payment> existingPayment =
            paymentRepository.findByCustomerIdAndIdempotencyKey(
                    request.getCustomerId(),
                    request.getIdempotencyKey());

    // STEP 2: If the key already exists, verify that the
    // new request contains the same payment details.
    if (existingPayment.isPresent()) {

        Payment existing = existingPayment.get();

        // Compare the important payment details.
        boolean sameRequest =
                existing.getAmount().compareTo(request.getAmount()) == 0
                && existing.getCurrency().equals(request.getCurrency())
                && existing.getPaymentMethod() == request.getPaymentMethod()
                && java.util.Objects.equals(
                        existing.getMerchantReference(),
                        request.getMerchantReference());

        // If the details differ, reject the reused key.
        if (!sameRequest) {
            throw new DuplicatePaymentException(
                    "Idempotency key has already been used with different payment details.");
        }

        // Same request: return the original payment.
        // Do not create another payment or history record.
        return paymentMapper.toResponse(existing);
    }

    // STEP 3: Convert the incoming DTO into a Payment entity.
    Payment payment = paymentMapper.toEntity(request);

    // STEP 4: Save the payment in the database.
    Payment savedPayment;

    try {
        savedPayment = paymentRepository.saveAndFlush(payment);

    } catch (org.springframework.dao.DataIntegrityViolationException exception) {

        // A database constraint may have been violated,
        // for example, a duplicate idempotency key.
        throw new DuplicatePaymentException(
                "A payment with this customer and idempotency key already exists.");
    }

    // STEP 5: Record the initial payment status.
    // A newly created payment starts with PENDING status.
    paymentStatusHistoryService.recordStatusChange(
            savedPayment,
            null,
            PaymentStatus.PENDING.name(),
            "Payment created successfully"
    );

    // STEP 6: Convert the saved entity into a response DTO.
    return paymentMapper.toResponse(savedPayment);
}


/**
 * Retrieves payment history and converts database entities into DTOs.
 *
 * Example:
 * getPaymentHistory(101L) retrieves the history of payment 101
 * and returns only the fields required by the API.
 *
 * @param paymentId database ID of the payment
 * @return list of payment history response DTOs
 */

/**
 * Retrieves payment history and converts database entities into DTOs.
 *
 * Example:
 * For payment ID 101, returns all recorded status changes
 * as PaymentStatusHistoryResponse objects.
 */

/**
 * Retrieves payment history and converts each database entity
 * into a PaymentStatusHistoryResponse DTO.
 *
 * Example:
 * Payment ID 101 has three history records.
 * This method converts all three records into DTO objects.
 */
/**
 * Retrieves payment history and converts entities into DcreTOs.
 *
 * Example:
 * Retrieves all status changes for payment ID 101
 * and returns them as response DTOs.
 */
//@Transactional(readOnly = true)
//public List<PaymentStatusHistoryResponse> getPaymentHistory(Long paymentId) {
//
//    // Retrieve database entities.
//    List<PaymentStatusHistory> historyRecords =
//            historyRepository.findByPaymentId(paymentId);
//
//    // Convert each entity into a response DTO using the mapper.
//    return historyRecords.stream()
//            .map(historyMapper::toResponse)
//            .toList();
//}


}
