package com.example.product_test.gateway.filter;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayFlowEntry;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.StreamGatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.UriWhiteListMatcher;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowItem;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.*;

/**
 * Sentinel Configuration for Gateway
 * 配置 Sentinel 流量治理规则（熔断、限流、降级）
 */
@Slf4j
@Configuration
public class SentinelGatewayConfig {

    @PostConstruct
    public void init() {
        log.info("[Sentinel] Initializing Gateway Sentinel configuration...");
        
        // 初始化限流规则
        initFlowRules();
        
        // 初始化降级规则
        initDegradeRules();
        
        // 初始化 API 分组规则
        initApiDefinitions();
    }

    /**
     * 初始化限流规则
     * 
     * 限流策略:
     * - resource: 资源名称
     * - count: 每秒允许的请求数
     * - grade: 限流维度 (1=QPS/0=并发线程数)
     * - limitApp: 关联的应用
     * - controlBehavior: 流控效果 (0=直接拒绝/1=排队等待/2=慢启动)
     */
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();
        
        // Product Service 限流规则
        FlowRule productRule = new FlowRule();
        productRule.setResource("product-service");
        productRule.setCount(100);  // QPS 100
        productRule.setGrade(1);  // QPS
        productRule.setLimitApp("default");
        productRule.setControlBehavior(0);  // 直接拒绝
        rules.add(productRule);
        
        // Order Service 限流规则
        FlowRule orderRule = new FlowRule();
        orderRule.setResource("order-service");
        orderRule.setCount(50);  // QPS 50
        orderRule.setGrade(1);  // QPS
        orderRule.setLimitApp("default");
        orderRule.setControlBehavior(0);  // 直接拒绝
        rules.add(orderRule);
        
        // User Service 限流规则
        FlowRule userRule = new FlowRule();
        userRule.setResource("user-service");
        userRule.setCount(50);  // QPS 50
        userRule.setGrade(1);  // QPS
        userRule.setLimitApp("default");
        userRule.setControlBehavior(0);  // 直接拒绝
        rules.add(userRule);
        
        // Backend Service 限流规则
        FlowRule backendRule = new FlowRule();
        backendRule.setResource("backend-service");
        backendRule.setCount(200);  // QPS 200
        backendRule.setGrade(1);  // QPS
        backendRule.setLimitApp("default");
        backendRule.setControlBehavior(0);  // 直接拒绝
        rules.add(backendRule);
        
        // 全局限流规则
        FlowRule allRule = new FlowRule();
        allRule.setResource("gateway-default");
        allRule.setCount(500);  // 总 QPS 500
        allRule.setGrade(1);  // QPS
        allRule.setLimitApp("default");
        allRule.setControlBehavior(0);  // 直接拒绝
        rules.add(allRule);
        
        FlowRuleManager.loadRules(rules);
        log.info("[Sentinel] Loaded {} flow rules", rules.size());
    }

    /**
     * 初始化降级规则（熔断）
     * 
     * 熔断策略:
     * - resource: 资源名称
     * - count: 阈值
     * - grade: 降级模式 (0=异常比例/1=异常数/2=响应时间)
     * - timeWindow: 熔断时长（秒）
     * - minRequestAmount: 最小请求数
     * - statIntervalMs: 统计时长（毫秒）
     * - slowRatioThreshold: 慢调用比例阈值
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();
        
        // Product Service 熔断规则 - 基于异常比例
        DegradeRule productDegradeRule = new DegradeRule();
        productDegradeRule.setResource("product-service");
        productDegradeRule.setCount(0.5);  // 异常比例 50%
        productDegradeRule.setGrade(0);  // 异常比例
        productDegradeRule.setTimeWindow(30);  // 熔断 30 秒
        productDegradeRule.setMinRequestAmount(5);  // 最小 5 个请求
        productDegradeRule.setStatIntervalMs(10000);  // 10 秒内统计
        rules.add(productDegradeRule);
        
        // Order Service 熔断规则 - 基于响应时间
        DegradeRule orderDegradeRule = new DegradeRule();
        orderDegradeRule.setResource("order-service");
        orderDegradeRule.setCount(2000);  // 响应时间 2000ms
        orderDegradeRule.setGrade(2);  // 响应时间
        orderDegradeRule.setTimeWindow(60);  // 熔断 60 秒
        orderDegradeRule.setMinRequestAmount(10);  // 最小 10 个请求
        orderDegradeRule.setSlowRatioThreshold(0.5);  // 50% 慢调用
        rules.add(orderDegradeRule);
        
        // Backend Service 熔断规则 - 基于异常数
        DegradeRule backendDegradeRule = new DegradeRule();
        backendDegradeRule.setResource("backend-service");
        backendDegradeRule.setCount(10);  // 异常数 10
        backendDegradeRule.setGrade(1);  // 异常数
        backendDegradeRule.setTimeWindow(60);  // 熔断 60 秒
        backendDegradeRule.setMinRequestAmount(5);  // 最小 5 个请求
        backendDegradeRule.setStatIntervalMs(60000);  // 1 分钟内统计
        rules.add(backendDegradeRule);
        
        DegradeRuleManager.loadRules(rules);
        log.info("[Sentinel] Loaded {} degrade rules", rules.size());
    }

    /**
     * 初始化 API 分组定义
     * 将多个 API 路径归为一组进行统一管理
     */
    private void initApiDefinitions() {
        Set<GatewayFlowEntry> entries = new HashSet<>();
        
        // Product API 分组
        GatewayFlowEntry productEntry = new GatewayFlowEntry();
        productEntry.setName("product-api");
        productEntry.setMatcher(new UriWhiteListMatcher(
                Arrays.asList("/product/**", "/products/**")
        ));
        entries.add(productEntry);
        
        // Order API 分组
        GatewayFlowEntry orderEntry = new GatewayFlowEntry();
        orderEntry.setName("order-api");
        orderEntry.setMatcher(new UriWhiteListMatcher(
                Arrays.asList("/order/**", "/orders/**", "/seckill/**")
        ));
        entries.add(orderEntry);
        
        // User API 分组
        GatewayFlowEntry userEntry = new GatewayFlowEntry();
        userEntry.setName("user-api");
        userEntry.setMatcher(new UriWhiteListMatcher(
                Arrays.asList("/user/**", "/users/**", "/auth/**")
        ));
        entries.add(userEntry);
        
        log.info("[Sentinel] Loaded {} API definitions", entries.size());
    }
}
