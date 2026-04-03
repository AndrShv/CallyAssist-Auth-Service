package com.example.project.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ApiMetricsService {

    private final Counter loginRequests;
    private final Counter registerRequests;

    public ApiMetricsService(MeterRegistry registry) {

        loginRequests = registry.counter("auth.api.login.requests");
        registerRequests = registry.counter("auth.api.register.requests");
    }

    public void loginRequest() {
        loginRequests.increment();
    }

    public void registerRequest() {
        registerRequests.increment();
    }
}
