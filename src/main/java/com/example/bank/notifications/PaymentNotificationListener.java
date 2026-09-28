package com.example.bank.notifications;

import com.example.bank.notifications.internal.NotificationRepository;
import com.example.bank.notifications.internal.SentNotification;
import com.example.bank.payments.PaymentCompleted;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
class PaymentNotificationListener {

    private final SmsGateway sms;
    private final NotificationRepository notifications;

    PaymentNotificationListener(SmsGateway sms, NotificationRepository notifications) {
        this.sms = sms;
        this.notifications = notifications;
    }

    /**
     * {@code @ApplicationModuleListener} = {@code @Async} +
     * {@code @TransactionalEventListener} + {@code @Transactional(REQUIRES_NEW)}.
     * It runs after the payment transaction commits, in a transaction of its own, so
     * a dead SMS gateway can never roll a payment back. If this method throws, the
     * event publication stays incomplete and can be resubmitted.
     */
    @ApplicationModuleListener
    void on(PaymentCompleted event) {

        sms.send(event.customerId(),
                "Payment of %s %s completed.".formatted(event.currency(), event.amount()));

        notifications.save(new SentNotification(event.paymentId(), event.customerId()));
    }
}
