/**
 * Customer master data. A leaf module: it depends on no other application module,
 * which is what the empty {@code allowedDependencies} declares and enforces.
 */
@ApplicationModule(
        displayName = "Customer Master",
        allowedDependencies = {})
package com.example.bank.customer;

import org.springframework.modulith.ApplicationModule;
