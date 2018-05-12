package com.revolut.test.health;

import com.codahale.metrics.health.HealthCheck;

public class RevolutHealthCheck extends HealthCheck {

    @Override
    protected Result check() {
        return Result.healthy();
    }
}
