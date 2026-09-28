package com.example.bank.notifications.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sent_notification")
public class SentNotification {

    @Id
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    protected SentNotification() { // required by JPA
    }

    public SentNotification(UUID paymentId, String customerId) {
        this.id = UUID.randomUUID();
        this.paymentId = paymentId;
        this.customerId = customerId;
        this.sentAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}
