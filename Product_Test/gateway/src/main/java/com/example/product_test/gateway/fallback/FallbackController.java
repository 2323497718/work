package com.example.product_test.gateway.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback Controller
 * 处理服务熔断、降级时的响应
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/product")
    public Mono<ResponseEntity<Map<String, Object>>> productFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", 503);
        response.put("message", "Product service is currently unavailable");
        response.put("timestamp", Instant.now().toString());
        response.put("fallback", true);
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/order")
    public Mono<ResponseEntity<Map<String, Object>>> orderFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", 503);
        response.put("message", "Order service is currently unavailable");
        response.put("timestamp", Instant.now().toString());
        response.put("fallback", true);
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/user")
    public Mono<ResponseEntity<Map<String, Object>>> userFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", 503);
        response.put("message", "User service is currently unavailable");
        response.put("timestamp", Instant.now().toString());
        response.put("fallback", true);
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/backend")
    public Mono<ResponseEntity<Map<String, Object>>> backendFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", 503);
        response.put("message", "Backend service is currently unavailable");
        response.put("timestamp", Instant.now().toString());
        response.put("fallback", true);
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/default")
    public Mono<ResponseEntity<Map<String, Object>>> defaultFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", 503);
        response.put("message", "Service temporarily unavailable, please try again later");
        response.put("timestamp", Instant.now().toString());
        response.put("fallback", true);
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}
