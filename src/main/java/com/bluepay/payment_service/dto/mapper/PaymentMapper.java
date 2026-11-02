package com.bluepay.payment_service.dto.mapper;

import org.springframework.stereotype.Component;

import com.bluepay.payment_service.dto.CreatePaymentRequest;
import com.bluepay.payment_service.dto.PaymentResponse;
import com.bluepay.payment_service.data.Payment;
import com.bluepay.payment_service.data.PaymentStatus;


@Component
public class PaymentMapper {

    /**
     * Converts an incoming request DTO into a Payment entity.
     */
    public Payment toEntity(CreatePaymentRequest request) {

        Payment payment = new Payment();

        // Copy client-provided payment details into the entity.
        payment.setCustomerId(request.getCustomerId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setMerchantReference(request.getMerchantReference());
        payment.setIdempotencyKey(request.getIdempotencyKey());

        // The server controls the initial payment status.
        payment.setStatus(PaymentStatus.PENDING);

        return payment;
    }

    /**
     * Converts a saved Payment entity into an API response DTO.
     */
    public PaymentResponse toResponse(Payment payment) {

        PaymentResponse response = new PaymentResponse();

        // Copy the payment details that are safe to return to the client.
        response.setId(payment.getId());
        response.setPaymentReference(payment.getPaymentReference());
        response.setCustomerId(payment.getCustomerId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus());
        response.setMerchantReference(payment.getMerchantReference());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());

        return response;
    }
}