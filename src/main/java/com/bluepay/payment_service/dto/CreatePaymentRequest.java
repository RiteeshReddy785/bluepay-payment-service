package com.bluepay.payment_service.dto;

import java.math.BigDecimal;

import com.bluepay.payment_service.data.PaymentMethod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Class: CreatePaymentRequest
 *
 * Purpose:
 * Receives payment details sent by a client when requesting
 * a new payment through the BluePay API.
 *
 * Example:
 * A customer wants to pay INR 500 using UPI.
 * The client sends customerId, amount, currency, paymentMethod
 * and an idempotencyKey in the API request.
 *
 * This DTO validates the input fields when validation is
 * triggered by the controller. It does not save data to MySQL.
 */
public class CreatePaymentRequest {

    // Identifies the customer requesting the payment.
    @NotBlank(message = "Customer ID is required")
    @Size(max = 100, message = "Customer ID cannot exceed 100 characters")
    private String customerId;

    // The payment amount must be greater than zero.
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", inclusive = false,
            message = "Amount must be greater than zero")
    @Digits(integer = 10, fraction = 2,
            message = "Amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal amount;

    // Three-letter uppercase currency code, for example INR or USD.
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$",
            message = "Currency must be a three-letter uppercase code")
    private String currency;

    // The payment method, such as UPI, CARD or WALLET.
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    // Optional reference supplied by a merchant.
    @Size(max = 100, message = "Merchant reference cannot exceed 100 characters")
    private String merchantReference;

    // Identifies a request so retries can be checked for duplicates later.
    @NotBlank(message = "Idempotency key is required")
    @Size(min = 16, max = 128,
            message = "Idempotency key must contain 16 to 128 characters")
    private String idempotencyKey;

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

    // Getter and setter for merchantReference.
    public String getMerchantReference() {
        return merchantReference;
    }

    public void setMerchantReference(String merchantReference) {
        this.merchantReference = merchantReference;
    }

    // Getter and setter for idempotencyKey.
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}