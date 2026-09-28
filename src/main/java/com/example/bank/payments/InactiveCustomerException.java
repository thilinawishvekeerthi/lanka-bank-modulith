package com.example.bank.payments;

public class InactiveCustomerException extends RuntimeException {

    public InactiveCustomerException(String customerId) {
        super("Customer " + customerId + " is not active");
    }
}
