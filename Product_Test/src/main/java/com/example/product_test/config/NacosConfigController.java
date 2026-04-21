package com.example.product_test.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nacos Configuration and Service Discovery Controller
 * 演示 Nacos 配置中心和注册发现功能
 */
@Slf4j
@RestController
@RequestMapping("/api/nacos")
@RequiredArgsConstructor
@RefreshScope
public class NacosConfigController {

    private final DiscoveryClient discoveryClient;
    private final AppProperties appProperties;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private int serverPort;

    /**
     * 获取当前服务实例信息
     */
    @GetMapping("/instance")
    public Map<String, Object> getInstanceInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("serviceName", applicationName);
        info.put("port", serverPort);
        info.put("instanceId", applicationName + "-" + serverPort);
        info.put("timestamp", Instant.now().toString());
        return info;
    }

    /**
     * 获取所有已注册的服务列表
     */
    @GetMapping("/services")
    public Map<String, Object> getServices() {
        Map<String, Object> result = new HashMap<>();
        List<String> services = discoveryClient.getServices();
        result.put("total", services.size());
        result.put("services", services);
        result.put("timestamp", Instant.now().toString());
        return result;
    }

    /**
     * 获取指定服务的所有实例
     */
    @GetMapping("/services/{serviceId}")
    public Map<String, Object> getServiceInstances(@PathVariable String serviceId) {
        Map<String, Object> result = new HashMap<>();
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        result.put("serviceId", serviceId);
        result.put("total", instances.size());
        result.put("instances", instances.stream().map(instance -> {
            Map<String, Object> info = new HashMap<>();
            info.put("instanceId", instance.getInstanceId());
            info.put("host", instance.getHost());
            info.put("port", instance.getPort());
            info.put("uri", instance.getUri().toString());
            info.put("metadata", instance.getMetadata());
            info.put("secure", instance.isSecure());
            return info;
        }).toList());
        result.put("timestamp", Instant.now().toString());
        return result;
    }

    /**
     * 获取动态配置属性
     * 测试 Nacos 配置中心的动态刷新功能
     */
    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("app", appProperties);
        config.put("timestamp", Instant.now().toString());
        config.put("note", "修改 Nacos 配置中心的值后，此处会显示更新后的值");
        return config;
    }

    /**
     * 测试限流配置
     */
    @GetMapping("/config/rate-limit")
    public Map<String, Object> getRateLimitConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", appProperties.getFeatures().getRateLimit().isEnabled());
        config.put("maxRequestsPerSecond", appProperties.getFeatures().getRateLimit().getMaxRequestsPerSecond());
        config.put("burstCapacity", appProperties.getFeatures().getRateLimit().getBurstCapacity());
        config.put("timestamp", Instant.now().toString());
        return config;
    }

    /**
     * 测试秒杀配置
     */
    @GetMapping("/config/seckill")
    public Map<String, Object> getSeckillConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", appProperties.getFeatures().getSeckill().isEnabled());
        config.put("maxQuantityPerOrder", appProperties.getFeatures().getSeckill().getMaxQuantityPerOrder());
        config.put("seckillDurationMinutes", appProperties.getFeatures().getSeckill().getSeckillDurationMinutes());
        config.put("timestamp", Instant.now().toString());
        return config;
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", applicationName);
        health.put("port", serverPort);
        health.put("timestamp", Instant.now().toString());
        return health;
    }
}
