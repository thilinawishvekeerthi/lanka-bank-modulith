package com.example.bank.notifications;

import com.example.bank.notifications.internal.NotificationRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notifications;

    NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    public long countForPayment(UUID paymentId) {
        return notifications.countByPaymentId(paymentId);
    }
}
