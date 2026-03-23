package com.example.product_test.product.mapper;

import com.example.product_test.product.model.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductMapper {

    @Select("SELECT id, product_name, price, status, created_at, updated_at FROM products WHERE id = #{id}")
    Product findById(Long id);
}
