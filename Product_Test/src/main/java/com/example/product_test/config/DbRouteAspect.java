package com.example.product_test.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DbRouteAspect {

    private static final Logger log = LoggerFactory.getLogger(DbRouteAspect.class);

    @Around("execution(public * com.example.product_test..service.impl.*.*(..))")
    public Object routeDb(ProceedingJoinPoint pjp) throws Throwable {
        String methodName = pjp.getSignature().getName();
        boolean isReadMethod = methodName.startsWith("get")
                || methodName.startsWith("find")
                || methodName.startsWith("list")
                || methodName.startsWith("search");
        try {
            if (isReadMethod) {
                DbContextHolder.useRead();
                log.info("db-route=READ method={}", methodName);
            } else {
                DbContextHolder.useWrite();
                log.info("db-route=WRITE method={}", methodName);
            }
            return pjp.proceed();
        } finally {
            DbContextHolder.clear();
        }
    }
}
