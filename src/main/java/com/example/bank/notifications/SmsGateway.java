package com.example.bank.notifications;

/**
 * The seam where the real SMS provider plugs in. An interface, so the module can be
 * tested without a provider and so a vendor swap touches one implementation class.
 */
public interface SmsGateway {

    void send(String customerId, String message);
}
