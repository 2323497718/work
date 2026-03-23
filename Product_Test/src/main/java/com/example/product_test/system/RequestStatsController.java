package com.example.product_test.system;

import com.example.product_test.common.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class RequestStatsController {

    private final RequestStatsHolder requestStatsHolder;

    @Value("${app.instance-id}")
    private String instanceId;

    public RequestStatsController(RequestStatsHolder requestStatsHolder) {
        this.requestStatsHolder = requestStatsHolder;
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        Map<String, Object> result = new HashMap<>();
        result.put("instanceId", instanceId);
        result.put("apiRequestCount", requestStatsHolder.getApiRequestCount());
        return ApiResponse.success(result);
    }
}
