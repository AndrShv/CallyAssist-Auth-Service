package com.example.project.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AuthMetricsService {

    private final Counter registerCounter;
    private final Counter loginCounter;
    private final Counter loginFailedCounter;
    private final Counter resetTokenCounter;
    private final Counter passwordResetCounter;

    public AuthMetricsService(MeterRegistry registry) {

        registerCounter = Counter.builder("auth.register.total")
                .description("Total registered users")
                .register(registry);

        loginCounter = Counter.builder("auth.login.success")
                .description("Successful login attempts")
                .register(registry);

        loginFailedCounter = Counter.builder("auth.login.failed")
                .description("Failed login attempts")
                .register(registry);

        resetTokenCounter = Counter.builder("auth.password.reset.token.sent")
                .description("Reset password tokens sent")
                .register(registry);

        passwordResetCounter = Counter.builder("auth.password.reset.completed")
                .description("Completed password resets")
                .register(registry);
    }

    public void incrementRegister() {
        registerCounter.increment();
    }

    public void incrementLoginSuccess() {
        loginCounter.increment();
    }

    public void incrementLoginFailed() {
        loginFailedCounter.increment();
    }

    public void incrementResetTokenSent() {
        resetTokenCounter.increment();
    }

    public void incrementPasswordReset() {
        passwordResetCounter.increment();
    }
}
