package com.bluepay.payment_service.dto;



import java.math.BigDecimal;
import java.time.Instant;

import com.bluepay.payment_service.data.PaymentMethod;
import com.bluepay.payment_service.data.PaymentStatus;

/**
 * Class: PaymentResponse
 *
 * Purpose:
 * Defines the payment information BluePay sends back to a client.
 *
 * Example:
 * After a client requests a payment of INR 500 using UPI,
 * BluePay can return the payment ID, payment reference,
 * amount, currency, payment method and current status.
 *
 * This DTO is used to control the information exposed by the API.
 * It does not represent a database table.
 */
public class PaymentResponse {

    // Database ID assigned to the saved payment.
    private Long id;

    // Unique reference generated for the payment.
    private String paymentReference;

    // Customer associated with the payment.
    private String customerId;

    // Payment amount.
    private BigDecimal amount;

    // Currency code, such as INR.
    private String currency;

    // Payment method, such as UPI or CARD.
    private PaymentMethod paymentMethod;

    // Current status of the payment.
    private PaymentStatus status;

    // Optional reference supplied by the merchant.
    private String merchantReference;

    // Time when the payment was created.
    private Instant createdAt;

    // Time when the payment was last updated.
    private Instant updatedAt;

    // Getter and setter for id.
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Getter and setter for paymentReference.
    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    // Getter and setter for customerId.
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    // Getter and setter for amount.
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    // Getter and setter for currency.
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    // Getter and setter for paymentMethod.
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // Getter and setter for status.
    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    // Getter and setter for merchantReference.
    public String getMerchantReference() {
        return merchantReference;
    }

    public void setMerchantReference(String merchantReference) {
        this.merchantReference = merchantReference;
    }

    // Getter and setter for createdAt.
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    // Getter and setter for updatedAt.
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}