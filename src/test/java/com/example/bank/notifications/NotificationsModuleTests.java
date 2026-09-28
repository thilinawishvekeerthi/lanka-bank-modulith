package com.example.bank.notifications;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.bank.payments.PaymentCompleted;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

/**
 * Notifications is driven by an event, so its test publishes that event directly.
 * No payments code runs here at all.
 */
@ApplicationModuleTest
class NotificationsModuleTests {

    private final NotificationService notifications;

    NotificationsModuleTests(NotificationService notifications) {
        this.notifications = notifications;
    }

    @Test
    void recordsNotificationForCompletedPayment(Scenario scenario) {

        var paymentId = UUID.randomUUID();
        var event = new PaymentCompleted(
                paymentId, "C-001", new BigDecimal("2500.00"), "LKR", Instant.now());

        scenario.publish(event)
                .andWaitForStateChange(() -> notifications.countForPayment(paymentId) > 0)
                .andVerify(__ -> assertThat(notifications.countForPayment(paymentId)).isEqualTo(1));
    }
}
