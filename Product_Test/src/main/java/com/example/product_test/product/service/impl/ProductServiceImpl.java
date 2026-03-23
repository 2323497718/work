package com.example.product_test.product.service.impl;

import com.example.product_test.product.mapper.ProductMapper;
import com.example.product_test.product.model.Product;
import com.example.product_test.product.service.ProductService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ProductServiceImpl implements ProductService {

    private static final String PRODUCT_CACHE_KEY = "product:detail:";
    private static final String PRODUCT_LOCK_KEY = "lock:product:";
    private static final String NULL_PLACEHOLDER = "NULL";
    private static final Duration NULL_TTL = Duration.ofMinutes(2);
    private static final Duration BASE_TTL = Duration.ofMinutes(30);

    private final ProductMapper productMapper;
    private final StringRedisTemplate redisTemplate;

    public ProductServiceImpl(ProductMapper productMapper, StringRedisTemplate redisTemplate) {
        this.productMapper = productMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Product getProductById(Long id) {
        String key = PRODUCT_CACHE_KEY + id;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            if (NULL_PLACEHOLDER.equals(cached)) {
                return null;
            }
            return decodeProduct(cached);
        }

        String lockKey = PRODUCT_LOCK_KEY + id;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(10));
        if (Boolean.TRUE.equals(locked)) {
            try {
                Product fromDb = productMapper.findById(id);
                if (fromDb == null) {
                    redisTemplate.opsForValue().set(key, NULL_PLACEHOLDER, NULL_TTL);
                    return null;
                }

                int jitter = ThreadLocalRandom.current().nextInt(1, 11);
                Duration ttl = BASE_TTL.plusMinutes(jitter);
                redisTemplate.opsForValue().set(key, encodeProduct(fromDb), ttl);
                return fromDb;
            } finally {
                redisTemplate.delete(lockKey);
            }
        }

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String retry = redisTemplate.opsForValue().get(key);
        if (retry == null || NULL_PLACEHOLDER.equals(retry)) {
            return null;
        }
        return decodeProduct(retry);
    }

    private String encodeProduct(Product product) {
        return product.getId() + "|" + product.getProductName() + "|" + product.getPrice() + "|" + product.getStatus();
    }

    private Product decodeProduct(String encoded) {
        String[] arr = encoded.split("\\|", -1);
        if (arr.length < 4) {
            throw new IllegalStateException("invalid cached product format");
        }
        Product product = new Product();
        product.setId(Long.parseLong(arr[0]));
        product.setProductName(arr[1]);
        product.setPrice(new BigDecimal(arr[2]));
        product.setStatus(Integer.parseInt(arr[3]));
        return product;
    }
}
