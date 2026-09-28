package com.example.bank.payments;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @param reference caller-supplied idempotency key. Retrying the same reference
 *                  returns the original payment instead of charging twice.
 */
public record PaymentRequest(
        @NotBlank String customerId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank String reference) {
}
