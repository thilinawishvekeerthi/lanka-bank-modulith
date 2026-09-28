/**
 * Customer notifications. Listens to payments; payments never calls back into it.
 * The dependency is declared here so it shows up in the generated documentation.
 */
@ApplicationModule(
        displayName = "Notifications",
        allowedDependencies = "payments")
package com.example.bank.notifications;

import org.springframework.modulith.ApplicationModule;
