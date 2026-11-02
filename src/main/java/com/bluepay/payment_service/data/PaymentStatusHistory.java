package com.bluepay.payment_service.data;



import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entity: PaymentStatusHistory
 *
 * Purpose:
 * Stores every status change made to a payment.
 *
 * Real-world example:
 * Payment 101 changes from PENDING to PROCESSING.
 * Later it changes from PROCESSING to SUCCEEDED.
 *
 * Both changes are stored as separate records in the
 * payment_status_history table.
 *
 * This allows BluePay to track the complete lifecycle
 * of a payment instead of storing only its latest status.
 */
@Entity
@Table(name = "payment_status_history")
public class PaymentStatusHistory {

    // Unique database ID for each history record.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The payment associated with this history record.
    // Multiple history records can belong to one payment.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    // Status before the change.
    // Example: PENDING
    @Column(name = "previous_status", length = 30)
    private String previousStatus;

    // Status after the change.
    // Example: PROCESSING
    @Column(name = "new_status", nullable = false, length = 30)
    private String newStatus;

    // Date and time when this status change occurred.
    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    // Explains why the status was changed.
    // Example: "Payment processing started".
    @Column(name = "reason", length = 500)
    private String reason;

    // JPA requires a no-argument constructor.
    public PaymentStatusHistory() {
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

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
