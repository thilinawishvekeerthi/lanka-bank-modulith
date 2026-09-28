package com.example.bank.payments;

import com.example.bank.customer.CustomerService;
import com.example.bank.payments.internal.Payment;
import com.example.bank.payments.internal.PaymentRepository;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private static final String CURRENCY = "LKR";

    private final PaymentRepository payments;
    private final CustomerService customers;
    private final ApplicationEventPublisher events;

    PaymentService(PaymentRepository payments,
                   CustomerService customers,
                   ApplicationEventPublisher events) {
        this.payments = payments;
        this.customers = customers;
        this.events = events;
    }

    /**
     * Writes the payment and the "PaymentCompleted must be delivered" record in one
     * transaction. Nothing here knows that notifications exist.
     */
    @Transactional
    public UUID pay(PaymentRequest request) {

        var existing = payments.findByReference(request.reference());

        if (existing.isPresent()) {
            return existing.get().getId(); // idempotent replay, no second event
        }

        if (!customers.isActive(request.customerId())) {
            throw new InactiveCustomerException(request.customerId());
        }

        var payment = payments.save(
                new Payment(request.customerId(), request.amount(), CURRENCY, request.reference()));

        events.publishEvent(new PaymentCompleted(
                payment.getId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getCompletedAt()));

        return payment.getId();
    }
}
