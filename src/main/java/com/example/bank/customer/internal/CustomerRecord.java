package com.example.bank.customer.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Internal state of the customer module. {@code public} for JPA's sake, but
 * unreachable from other modules: {@code ApplicationModules.verify()} rejects
 * any reference to a {@code ..internal..} type from outside this module.
 */
@Entity
@Table(name = "customer")
public class CustomerRecord {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false, unique = true)
    private String customerId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private boolean active;

    protected CustomerRecord() { // required by JPA
    }

    public CustomerRecord(String customerId, String fullName, boolean active) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.fullName = fullName;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isActive() {
        return active;
    }
}
