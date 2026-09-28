package com.example.bank.customer.internal;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerRecord, UUID> {

    Optional<CustomerRecord> findByCustomerId(String customerId);
}
