package com.bluepay.payment_service.dto.mapper;




import org.springframework.stereotype.Component;

import com.bluepay.payment_service.dto.PaymentStatusHistoryResponse;
import com.bluepay.payment_service.data.PaymentStatusHistory;

/**
 * Mapper: PaymentStatusHistoryMapper
 *
 * Purpose:
 * Converts PaymentStatusHistory entities into DTOs.
 *
 * Real-world example:
 * A database record containing PENDING -> PROCESSING
 * is converted into a PaymentStatusHistoryResponse object
 * that can safely be returned by the REST API.
 */
@Component
public class PaymentStatusHistoryMapper {

    /**
     * Converts a payment history entity into a response DTO.
     *
     * @param history database entity
     * @return response DTO containing the required history fields
     */
    public PaymentStatusHistoryResponse toResponse(
            PaymentStatusHistory history) {

        // Create a new response object.
        PaymentStatusHistoryResponse response =
                new PaymentStatusHistoryResponse();

        // Copy the previous payment status.
        response.setPreviousStatus(history.getPreviousStatus());

        // Copy the new payment status.
        response.setNewStatus(history.getNewStatus());

        // Copy the status change timestamp.
        response.setChangedAt(history.getChangedAt());

        // Copy the reason for the status change.
        response.setReason(history.getReason());

        // Return the completed DTO.
        return response;
    }
}
