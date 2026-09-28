package com.example.bank.payments.web;

import com.example.bank.payments.InactiveCustomerException;
import com.example.bank.payments.PaymentRequest;
import com.example.bank.payments.PaymentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Package-private on purpose. The web adapter is an implementation detail of the
 * payments module; other modules call {@code PaymentService}, not HTTP.
 */
@RestController
@RequestMapping("/api/payments")
class PaymentController {

    private final PaymentService paymentService;

    PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    ResponseEntity<Map<String, UUID>> pay(@Valid @RequestBody PaymentRequest request) {

        UUID paymentId = paymentService.pay(request);

        return ResponseEntity.created(URI.create("/api/payments/" + paymentId))
                .body(Map.of("paymentId", paymentId));
    }

    @ExceptionHandler(InactiveCustomerException.class)
    ProblemDetail handleInactiveCustomer(InactiveCustomerException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }
}
