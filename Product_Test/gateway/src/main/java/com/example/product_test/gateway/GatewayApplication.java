package com.example.product_test.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.factory.FallbackGatewayFilterFactory;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

/**
 * API Gateway Application
 * 
 * 功能特性:
 * - 服务注册与发现 (Nacos)
 * - 动态路由配置 (Nacos Config)
 * - 流量治理 (Sentinel)
 * - 限流 (Redis RateLimiter)
 * - 熔断降级 (Spring Cloud CircuitBreaker)
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    /**
     * 自定义路径限流 Key 解析器
     * 基于请求路径进行限流
     */
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver pathKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getURI().getPath();
            return Mono.just(path);
        };
    }

    /**
     * IP 限流 Key 解析器
     * 基于客户端 IP 进行限流
     */
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress() != null 
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() 
                : "unknown";
            return Mono.just(ip);
        };
    }

    /**
     * 用户 ID 限流 Key 解析器
     * 基于请求头中的用户 ID 进行限流
     */
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            return Mono.just(userId != null ? userId : "anonymous");
        };
    }

    /**
     * API 组合 Key 解析器
     * 组合 IP + 路径进行限流
     */
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver compositeKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress() != null 
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() 
                : "unknown";
            String path = exchange.getRequest().getURI().getPath();
            return Mono.just(ip + "_" + path);
        };
    }
}
