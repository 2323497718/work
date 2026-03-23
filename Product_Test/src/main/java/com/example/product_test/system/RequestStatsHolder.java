package com.example.product_test.system;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class RequestStatsHolder {

    private final AtomicLong apiRequestCount = new AtomicLong(0);

    public long incrementAndGet() {
        return apiRequestCount.incrementAndGet();
    }

    public long getApiRequestCount() {
        return apiRequestCount.get();
    }
}
