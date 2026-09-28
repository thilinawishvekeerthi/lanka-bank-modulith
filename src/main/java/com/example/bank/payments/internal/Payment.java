package com.example.bank.payments.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment")
public class Payment {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    /** Idempotency key. The unique index is what actually stops double charges. */
    @Column(nullable = false, unique = true)
    private String reference;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    protected Payment() { // required by JPA
    }

    public Payment(String customerId, BigDecimal amount, String currency, String reference) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.reference = reference;
        this.completedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getReference() {
        return reference;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
