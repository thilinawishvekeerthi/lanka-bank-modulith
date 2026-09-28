package com.example.bank.notifications.internal;

import com.example.bank.notifications.SmsGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Stand-in for the real provider. Swap this class, not the listener. */
@Component
class LoggingSmsGateway implements SmsGateway {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsGateway.class);

    @Override
    public void send(String customerId, String message) {
        log.info("SMS to {}: {}", customerId, message);
    }
}
