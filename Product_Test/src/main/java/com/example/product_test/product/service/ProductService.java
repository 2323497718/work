package com.example.product_test.product.service;

import com.example.product_test.product.model.Product;

import java.util.List;

public interface ProductService {
    Product getProductById(Long id);

    List<Product> searchProducts(String keyword);

    Product createProduct(Product product);
}
