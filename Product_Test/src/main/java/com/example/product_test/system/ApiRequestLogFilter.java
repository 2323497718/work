package com.example.product_test.system;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiRequestLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiRequestLogFilter.class);

    private final RequestStatsHolder requestStatsHolder;

    @Value("${app.instance-id}")
    private String instanceId;

    public ApiRequestLogFilter(RequestStatsHolder requestStatsHolder) {
        this.requestStatsHolder = requestStatsHolder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/")) {
            long count = requestStatsHolder.incrementAndGet();
            log.info("instance={} method={} uri={} count={}", instanceId, request.getMethod(), request.getRequestURI(), count);
        }
        filterChain.doFilter(request, response);
    }
}
