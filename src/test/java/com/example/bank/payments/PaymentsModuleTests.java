package com.example.bank.payments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import com.example.bank.customer.CustomerService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Boots the payments module alone. Nothing from notifications is in this context, so a
 * broken notification cannot make a payments test fail.
 */
@ApplicationModuleTest
class PaymentsModuleTests {

    @MockitoBean
    CustomerService customerService; // the one allowed dependency, stubbed out

    private final PaymentService payments;

    PaymentsModuleTests(PaymentService payments) {
        this.payments = payments;
    }

    @Test
    void publishesPaymentCompleted(Scenario scenario) {

        when(customerService.isActive("C-001")).thenReturn(true);

        scenario.stimulate(() -> payments.pay(
                        new PaymentRequest("C-001", new BigDecimal("2500.00"), "REF-1")))
                .andWaitForEventOfType(PaymentCompleted.class)
                .matching(event -> event.customerId().equals("C-001"))
                .toArriveAndVerify(event -> {
                    assertThat(event.amount()).isEqualByComparingTo("2500.00");
                    assertThat(event.currency()).isEqualTo("LKR");
                });
    }

    @Test
    void rejectsInactiveCustomer() {

        when(customerService.isActive("C-999")).thenReturn(false);

        assertThatExceptionOfType(InactiveCustomerException.class).isThrownBy(
                () -> payments.pay(new PaymentRequest("C-999", new BigDecimal("100.00"), "REF-2")));
    }

    @Test
    void replayingTheSameReferenceDoesNotChargeTwice() {

        when(customerService.isActive("C-002")).thenReturn(true);

        var request = new PaymentRequest("C-002", new BigDecimal("750.00"), "REF-IDEMPOTENT");

        var first = payments.pay(request);
        var second = payments.pay(request);

        assertThat(second).isEqualTo(first);
    }
}
