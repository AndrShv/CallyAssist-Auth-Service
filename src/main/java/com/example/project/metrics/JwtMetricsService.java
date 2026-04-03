package com.example.project.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class JwtMetricsService {

    private final Counter jwtGenerated;
    private final Counter jwtValidated;
    private final Counter jwtInvalid;

    public JwtMetricsService(MeterRegistry registry) {

        jwtGenerated = registry.counter("auth.jwt.generated");
        jwtValidated = registry.counter("auth.jwt.validated");
        jwtInvalid = registry.counter("auth.jwt.invalid");
    }

    public void incrementGenerated() {
        jwtGenerated.increment();
    }

    public void incrementValidated() {
        jwtValidated.increment();
    }

    public void incrementInvalid() {
        jwtInvalid.increment();
    }
}
