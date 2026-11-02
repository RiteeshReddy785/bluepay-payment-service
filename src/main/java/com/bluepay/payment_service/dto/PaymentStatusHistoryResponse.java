package com.bluepay.payment_service.dto;

import java.time.Instant;

/**
 * DTO: PaymentStatusHistoryResponse
 *
 * Purpose:
 * Defines the information returned by BluePay when someone
 * requests a payment's status history.
 *
 * Real-world example:
 * If payment 101 changes from PENDING to PROCESSING,
 * the API returns the previous status, new status,
 * timestamp and reason.
 *
 * This DTO prevents the API from exposing the complete
 * PaymentStatusHistory database entity.
 */
public class PaymentStatusHistoryResponse {

    // Payment status before the transition.
    private String previousStatus;

    // Payment status after the transition.
    private String newStatus;

    // Date and time when the transition occurred.
    private Instant changedAt;

    // Explanation for why the status changed.
    private String reason;

    // Getters and setters

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}