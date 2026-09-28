package com.example.bank.payments;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Part of the payments API on purpose: other modules are allowed to depend on the
 * event, never on the entity behind it. Keep it immutable and self-contained -
 * a listener may run minutes later, after a restart, so it must not need a
 * database lookup into payments to make sense of the event.
 */
public record PaymentCompleted(
        UUID paymentId,
        String customerId,
        BigDecimal amount,
        String currency,
        Instant completedAt) {
}
