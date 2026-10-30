package com.cricket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Cricket Score Management System.
 * Enables scheduling for potential background simulation tasks.
 */
@SpringBootApplication
@EnableScheduling
public class CricketApplication {
    public static void main(String[] args) {
        SpringApplication.run(CricketApplication.class, args);
        System.out.println("\n✅ Cricket Score System running at: http://localhost:8080\n");
    }
}
