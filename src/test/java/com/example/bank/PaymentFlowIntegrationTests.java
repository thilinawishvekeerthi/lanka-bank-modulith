package com.example.bank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.awaitility.Awaitility.await;

import com.example.bank.notifications.NotificationService;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * The whole monolith in one process: HTTP in, payment stored, event published, SMS sent,
 * publication marked complete. A nine-service version of the same feature needs
 * docker-compose before it can assert any of this.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("demo")
class PaymentFlowIntegrationTests {

    @LocalServerPort
    int port;

    @Autowired
    NotificationService notifications;

    @Autowired
    JdbcTemplate jdbc;

    private RestClient rest;

    @BeforeEach
    void setUp() {
        rest = RestClient.create("http://localhost:" + port);
    }

    @Test
    void completesPaymentAndNotifiesCustomer() {

        var response = rest.post()
                .uri("/api/payments")
                .body(Map.of("customerId", "C-001",
                        "amount", new BigDecimal("2500.00"),
                        "reference", "REF-INT-1"))
                .retrieve()
                .toEntity(Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var paymentId = UUID.fromString((String) response.getBody().get("paymentId"));

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(notifications.countForPayment(paymentId)).isEqualTo(1));

        // Nothing left in the outbox, and with completion-mode ARCHIVE the row moved aside
        assertThat(incompletePublications()).isZero();
        assertThat(archivedPublications()).isEqualTo(1);
    }

    @Test
    void rejectsPaymentForClosedAccount() {

        assertThatExceptionOfType(HttpClientErrorException.Conflict.class).isThrownBy(() ->
                rest.post()
                        .uri("/api/payments")
                        .body(Map.of("customerId", "C-999",
                                "amount", new BigDecimal("100.00"),
                                "reference", "REF-INT-2"))
                        .retrieve()
                        .toBodilessEntity());
    }

    @Test
    void rejectsMalformedRequest() {

        assertThatExceptionOfType(HttpClientErrorException.BadRequest.class).isThrownBy(() ->
                rest.post()
                        .uri("/api/payments")
                        .body(Map.of("customerId", "", "amount", new BigDecimal("0.00"),
                                "reference", ""))
                        .retrieve()
                        .toBodilessEntity());
    }

    private long incompletePublications() {
        return jdbc.queryForObject(
                "select count(*) from event_publication where completion_date is null", Long.class);
    }

    private long archivedPublications() {
        return jdbc.queryForObject("select count(*) from event_publication_archive", Long.class);
    }
}
