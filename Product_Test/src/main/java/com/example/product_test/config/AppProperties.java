package com.example.product_test.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Dynamic Configuration Properties
 * 支持 Nacos 动态刷新
 * 
 * 当 Nacos 配置中心的值发生变化时，这些属性会自动更新
 * 无需重启服务
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String name = "product-test-service";
    private String version = "1.0.0";
    private FeatureFlags features = new FeatureFlags();
    private BusinessConfig business = new BusinessConfig();

    @Data
    public static class FeatureFlags {
        private SeckillConfig seckill = new SeckillConfig();
        private CacheConfig cache = new CacheConfig();
        private RateLimitConfig rateLimit = new RateLimitConfig();

        @Data
        public static class SeckillConfig {
            private boolean enabled = true;
            private int maxQuantityPerOrder = 5;
            private int seckillDurationMinutes = 60;
        }

        @Data
        public static class CacheConfig {
            private boolean enabled = true;
            private int ttlSeconds = 3600;
            private boolean preloadEnabled = false;
        }

        @Data
        public static class RateLimitConfig {
            private boolean enabled = true;
            private int maxRequestsPerSecond = 100;
            private int burstCapacity = 200;
        }
    }

    @Data
    public static class BusinessConfig {
        private OrderConfig order = new OrderConfig();
        private ProductConfig product = new ProductConfig();
        private InventoryConfig inventory = new InventoryConfig();

        @Data
        public static class OrderConfig {
            private int timeoutSeconds = 300;
            private int maxRetryCount = 3;
            private String paymentUrl = "http://payment-service/api/pay";
        }

        @Data
        public static class ProductConfig {
            private int defaultPageSize = 20;
            private int maxPageSize = 100;
            private String cacheKeyPrefix = "product:";
        }

        @Data
        public static class InventoryConfig {
            private boolean checkBeforeOrder = true;
            private boolean autoDeduct = true;
        }
    }
}
