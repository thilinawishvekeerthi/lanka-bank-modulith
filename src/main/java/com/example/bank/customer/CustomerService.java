package com.example.bank.customer;

import com.example.bank.customer.internal.CustomerRecord;
import com.example.bank.customer.internal.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The module's public API. Everything another module needs from {@code customer}
 * goes through this class and {@link CustomerSummary}.
 */
@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customers;

    CustomerService(CustomerRepository customers) {
        this.customers = customers;
    }

    public boolean isActive(String customerId) {
        return customers.findByCustomerId(customerId)
                .map(CustomerRecord::isActive)
                .orElse(false);
    }

    public CustomerSummary find(String customerId) {
        return customers.findByCustomerId(customerId)
                .map(it -> new CustomerSummary(it.getCustomerId(), it.getFullName(), it.isActive()))
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }
}
