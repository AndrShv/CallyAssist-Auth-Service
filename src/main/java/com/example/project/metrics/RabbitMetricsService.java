package com.example.project.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class RabbitMetricsService {

    private final Counter messagesSent;

    public RabbitMetricsService(MeterRegistry registry) {
        messagesSent = registry.counter("auth.rabbit.messages.sent");
    }

    public void increment() {
        messagesSent.increment();
    }
}