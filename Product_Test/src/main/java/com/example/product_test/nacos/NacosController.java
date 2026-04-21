package com.example.product_test.nacos;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResource;
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
@RefreshScope
public class NacosController {

    private final DiscoveryClient discoveryClient;

    @Value("${spring.application.name:product-test-service}")
    private String applicationName;

    @Value("${server.port:8081}")
    private int serverPort;

    @Value("${app.features.rate-limit.max-requests-per-second:100}")
    private int maxRequestsPerSecond;

    @Value("${app.features.rate-limit.burst-capacity:200}")
    private int burstCapacity;

    @Value("${app.features.seckill.enabled:true}")
    private boolean seckillEnabled;

    @Value("${app.features.seckill.max-quantity-per-order:5}")
    private int maxQuantityPerOrder;

    public NacosController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

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
        info.put("status", "UP");
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
        log.info("[Nacos] Found {} registered services", services.size());
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
        config.put("applicationName", applicationName);
        config.put("port", serverPort);
        config.put("rateLimit", Map.of(
            "maxRequestsPerSecond", maxRequestsPerSecond,
            "burstCapacity", burstCapacity
        ));
        config.put("seckill", Map.of(
            "enabled", seckillEnabled,
            "maxQuantityPerOrder", maxQuantityPerOrder
        ));
        config.put("timestamp", Instant.now().toString());
        config.put("note", "修改 Nacos 配置中心的值后，此处会显示更新后的值（需要调用刷新接口）");
        return config;
    }

    /**
     * 测试限流配置 - 带 Sentinel 限流保护
     */
    @SentinelResource(value = "rateLimitConfig", blockHandler = "rateLimitBlockHandler")
    @GetMapping("/config/rate-limit")
    public Map<String, Object> getRateLimitConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("maxRequestsPerSecond", maxRequestsPerSecond);
        config.put("burstCapacity", burstCapacity);
        config.put("timestamp", Instant.now().toString());
        return config;
    }

    /**
     * 测试秒杀配置
     */
    @SentinelResource(value = "seckillConfig", blockHandler = "seckillBlockHandler")
    @GetMapping("/config/seckill")
    public Map<String, Object> getSeckillConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", seckillEnabled);
        config.put("maxQuantityPerOrder", maxQuantityPerOrder);
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

    /**
     * 限流处理方法
     */
    public Map<String, Object> rateLimitBlockHandler(BlockException ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", 429);
        result.put("message", "请求过于频繁，请稍后重试");
        result.put("timestamp", Instant.now().toString());
        return result;
    }

    /**
     * 秒杀限流处理方法
     */
    public Map<String, Object> seckillBlockHandler(BlockException ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", 429);
        result.put("message", "秒杀请求过于频繁，请稍后重试");
        result.put("timestamp", Instant.now().toString());
        return result;
    }
}
