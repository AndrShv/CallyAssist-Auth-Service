package com.example.project.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class EmailMetricsService {

    private final Counter emailSent;
    private final Counter emailFailed;

    public EmailMetricsService(MeterRegistry registry) {

        emailSent = registry.counter("auth.email.sent");
        emailFailed = registry.counter("auth.email.failed");
    }

    public void sent() {
        emailSent.increment();
    }

    public void failed() {
        emailFailed.increment();
    }
}
