package com.bluepay.payment_service.data;




// Java classes for monetary values, timestamps and unique references.
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// JPA annotations used to map this class to a MySQL database table.
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Version;

// Jakarta Bean Validation annotations for validating field values.
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Represents a payment transaction in the BluePay platform.
 *
 * Responsibilities:
 * 1. Store payment information in the database.
 * 2. Track payment status and lifecycle timestamps.
 * 3. Support idempotency to help prevent duplicate payment requests.
 * 4. Support optimistic locking for concurrent database updates.
 *
 * Note: This entity represents a payment record. It does not itself
 * execute a bank transfer or charge a real payment method.
 */
@Entity
@Table(
    name = "payments",

    // Indexes help MySQL find commonly queried payment records faster.
    indexes = {
        @Index(
            name = "idx_payment_customer",
            columnList = "customer_id"
        ),
        @Index(
            name = "idx_payment_status_created",
            columnList = "status, created_at"
        ),
        @Index(
            name = "idx_payment_reference",
            columnList = "payment_reference"
        )
    },

    // Unique constraints protect important identifiers at database level.
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_payment_reference",
            columnNames = "payment_reference"
        ),
        @UniqueConstraint(
            name = "uk_payment_idempotency",
            columnNames = {"customer_id", "idempotency_key"}
        )
    },

    // The database should reject non-positive payment amounts.
    // Verify generated DDL against the selected Hibernate and MySQL versions.
    check = {
        @CheckConstraint(
            name = "chk_payment_amount_positive",
            constraint = "amount > 0"
        )
    }
)
public class Payment {

    /**
     * Internal database primary key.
     * MySQL generates this value when a new payment is inserted.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Public payment identifier used by APIs and other services.
     * Generated automatically and made immutable after insertion.
     */
    @NotNull
    @Column(
        name = "payment_reference",
        nullable = false,
        updatable = false,
        length = 36
    )
    private String paymentReference;

    /**
     * Identifies the customer associated with this payment.
     * In the complete platform, this should reference a trusted customer
     * identity established by authentication and account services.
     */
    @NotBlank
    @Size(max = 100)
    @Column(
        name = "customer_id",
        nullable = false,
        length = 100
    )
    private String customerId;

    /**
     * Monetary amount for the payment.
     * BigDecimal avoids the binary floating-point precision issues
     * associated with float and double.
     *
     * Precision 12 and scale 2 allow up to 10 integer digits and
     * 2 fractional digits. Currency-specific rules may differ.
     */
    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    @Column(
        name = "amount",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal amount;

    /**
     * ISO-style three-letter currency code, for example INR or USD.
     * A format check alone does not prove that a currency is supported.
     */
    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$")
    @Column(
        name = "currency",
        nullable = false,
        length = 3
    )
    private String currency;

    /**
     * Payment method selected for the transaction.
     * EnumType.STRING stores values such as CARD or UPI in MySQL,
     * rather than storing numeric enum positions.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
        name = "payment_method",
        nullable = false,
        length = 30
    )
    private PaymentMethod paymentMethod;

    /**
     * Current state of the payment lifecycle.
     * Business logic must control which status transitions are allowed.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
        name = "status",
        nullable = false,
        length = 30
    )
    private PaymentStatus status;

    /**
     * Optional reference supplied by a merchant or calling system.
     * Useful for reconciliation and tracing requests across services.
     */
    @Size(max = 100)
    @Column(
        name = "merchant_reference",
        length = 100
    )
    private String merchantReference;

    /**
     * Client-provided idempotency key.
     * A unique customer/key pair helps prevent the same logical request
     * from creating multiple payment records.
     *
     * The service must still compare repeated requests and return the
     * original result only when the payload is equivalent.
     */
    @NotBlank
    @Size(min = 16, max = 128)
    @Column(
        name = "idempotency_key",
        nullable = false,
        length = 128
    )
    private String idempotencyKey;

    /**
     * Machine-readable reason when a payment fails.
     * Examples might include PAYMENT_DECLINED or PROVIDER_TIMEOUT.
     */
    @Size(max = 50)
    @Column(
        name = "failure_code",
        length = 50
    )
    private String failureCode;

    /**
     * Safe, human-readable failure description.
     * Never store secrets, card data, credentials or sensitive provider
     * responses in this field.
     */
    @Size(max = 500)
    @Column(
        name = "failure_reason",
        length = 500
    )
    private String failureReason;

    /**
     * Time when this record was first created.
     * Instant represents a point on the UTC timeline.
     */
    @NotNull
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private Instant createdAt;

    /**
     * Time when this record was most recently updated.
     * Updated automatically before a JPA update operation.
     */
    @NotNull
    @Column(
        name = "updated_at",
        nullable = false
    )
    private Instant updatedAt;

    /**
     * Optimistic locking version.
     * JPA checks this value during updates to detect conflicting writes.
     * It is not a substitute for correct payment or balance transactions.
     */
    @Version
    @Column(
        name = "version",
        nullable = false
    )
    private Long version;

    /**
     * JPA requires a no-argument constructor.
     * Protected visibility discourages ordinary application code
     * from creating incomplete entities directly.
     */
    public Payment() {
    }

    /**
     * Lifecycle callback invoked before the entity is first inserted.
     * Generates internal metadata when it has not already been assigned.
     */
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        // Generate a unique public reference for this payment.
        if (paymentReference == null) {
            paymentReference = UUID.randomUUID().toString();
        }

        // Set the creation timestamp only once.
        if (createdAt == null) {
            createdAt = now;
        }

        // Initialize the last-updated timestamp.
        updatedAt = now;

        // New payment records begin in the PENDING state.
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
    }

    /**
     * Lifecycle callback invoked before a JPA update.
     * Refreshes the last-updated timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Read-only access to the generated database ID.
    public Long getId() {
        return id;
    }

    // Read-only access to the public payment reference.
    public String getPaymentReference() {
        return paymentReference;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getMerchantReference() {
        return merchantReference;
    }

    public void setMerchantReference(String merchantReference) {
        this.merchantReference = merchantReference;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    // Failure details are read-only here; service logic should control them.
    public String getFailureCode() {
        return failureCode;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}
