package com.example.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Single deployable unit. The modules live in the direct sub-packages of this
 * class' package, which is what Spring Modulith uses as its module detection root.
 */
@Modulithic(systemName = "Lanka Bank Core")
@EnableAsync // @ApplicationModuleListener is @Async; without this it runs on the caller thread
@SpringBootApplication
public class BankApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankApplication.class, args);
    }
}
