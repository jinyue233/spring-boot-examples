package com.neo.healthindicator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class HealthIndicatorComponent implements HealthIndicator {

    public HealthIndicatorComponent(){
        System.out.println("===================HealthIndicatorComponent===============");
    }

    @Override
    public Health health() {

        return null;
    }
}
