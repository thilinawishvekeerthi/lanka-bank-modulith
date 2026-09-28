/**
 * Payment initiation. May talk to {@code customer} and to nothing else: adding an
 * import of {@code notifications} here turns the modularity test red.
 */
@ApplicationModule(
        displayName = "Payments",
        allowedDependencies = "customer")
package com.example.bank.payments;

import org.springframework.modulith.ApplicationModule;
