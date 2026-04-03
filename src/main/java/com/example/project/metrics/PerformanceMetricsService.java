package com.example.project.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class PerformanceMetricsService {

    private final Timer loginTimer;
    private final Timer registerTimer;
    private final Timer sendResetTokenTimer;
    private final Timer resetPasswordTimer;


    public PerformanceMetricsService(MeterRegistry registry) {
        loginTimer = registry.timer("auth.login.duration");
        registerTimer = registry.timer("auth.register.duration");
        sendResetTokenTimer = registry.timer("auth.sendResetToken.duration");
        resetPasswordTimer = registry.timer("auth.resetPassword.duration");
    }

    public Timer loginTimer() {
        return loginTimer;
    }

    public Timer registerTimer() {
        return registerTimer;
    }
    public Timer sendResetTokenTimer() {
        return sendResetTokenTimer;
    }
    public Timer resetPasswordTimer() {
        return resetPasswordTimer;
    }
}
