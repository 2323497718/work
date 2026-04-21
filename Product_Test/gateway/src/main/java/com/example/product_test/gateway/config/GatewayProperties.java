package com.example.product_test.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Gateway Configuration Properties
 * 支持 Nacos 动态刷新
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {

    private boolean enabled = true;
    private int maxRequestPerSecond = 1000;
    private int connectionTimeout = 30000;
    private int responseTimeout = 30000;
    private RetryConfig retry = new RetryConfig();
    private RateLimitConfig rateLimit = new RateLimitConfig();

    @Data
    public static class RetryConfig {
        private boolean enabled = true;
        private int maxAttempts = 3;
        private int backoffDelay = 100;
    }

    @Data
    public static class RateLimitConfig {
        private boolean enabled = true;
        private int defaultQps = 100;
        private int defaultBurstCapacity = 200;
        private String keyResolver = "path";
    }
}
