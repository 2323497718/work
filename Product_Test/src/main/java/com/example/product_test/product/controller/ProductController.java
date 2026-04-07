package com.example.product_test.product.controller;

import com.example.product_test.common.ApiResponse;
import com.example.product_test.product.model.Product;
import com.example.product_test.product.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public ApiResponse<Product> getById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ApiResponse.fail("product not found");
        }
        return ApiResponse.success(product);
    }

    @GetMapping("/search")
    public ApiResponse<List<Product>> search(@RequestParam String keyword) {
        return ApiResponse.success(productService.searchProducts(keyword));
    }

    @PostMapping
    public ApiResponse<Product> create(@RequestBody Product product) {
        return ApiResponse.success(productService.createProduct(product));
    }
}
