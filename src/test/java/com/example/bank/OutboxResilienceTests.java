package com.example.bank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import com.example.bank.notifications.NotificationService;
import com.example.bank.notifications.SmsGateway;
import com.example.bank.payments.PaymentRequest;
import com.example.bank.payments.PaymentService;
import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Proves the part people do not believe until they see it: when the downstream side
 * fails, the payment still commits, and the undelivered event is still on the books.
 */
@SpringBootTest
@ActiveProfiles("demo")
class OutboxResilienceTests {

    @MockitoBean
    SmsGateway smsGateway;

    @Autowired
    PaymentService payments;

    @Autowired
    NotificationService notifications;

    @Autowired
    IncompleteEventPublications incompletePublications;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void failedListenerLeavesPublicationForResubmission() {

        doThrow(new IllegalStateException("SMS gateway unreachable"))
                .when(smsGateway).send(anyString(), anyString());

        var paymentId = payments.pay(
                new PaymentRequest("C-002", new BigDecimal("750.00"), "REF-OUTBOX-1"));

        // The payment itself committed: the SMS failure did not roll it back
        assertThat(paymentId).isNotNull();

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(incompleteCount()).isEqualTo(1));

        assertThat(notifications.countForPayment(paymentId)).isZero();

        // Gateway recovers, operator resubmits, the notification finally lands
        doNothing().when(smsGateway).send(anyString(), anyString());
        incompletePublications.resubmitIncompletePublications(__ -> true);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    assertThat(notifications.countForPayment(paymentId)).isEqualTo(1);
                    assertThat(incompleteCount()).isZero();
                });
    }

    private long incompleteCount() {
        return jdbc.queryForObject(
                "select count(*) from event_publication where completion_date is null", Long.class);
    }
}
