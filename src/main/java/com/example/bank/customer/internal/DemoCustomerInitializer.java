package com.example.bank.customer.internal;

import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Demo data, seeded by the module that owns the table. Another module adding rows to
 * {@code customer} would be exactly the shared-database coupling this design avoids.
 */
@Profile("demo")
@Component
class DemoCustomerInitializer implements ApplicationRunner {

    private final CustomerRepository customers;

    DemoCustomerInitializer(CustomerRepository customers) {
        this.customers = customers;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        if (customers.count() > 0) {
            return;
        }

        customers.saveAll(List.of(
                new CustomerRecord("C-001", "Nimal Perera", true),
                new CustomerRecord("C-002", "Kamala Silva", true),
                new CustomerRecord("C-999", "Closed Account", false)));
    }
}
