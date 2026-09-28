package com.example.bank.customer;

/**
 * The shape other modules are allowed to see. The JPA entity stays inside
 * {@code customer.internal} so that persistence details cannot leak.
 */
public record CustomerSummary(String customerId, String fullName, boolean active) {
}
