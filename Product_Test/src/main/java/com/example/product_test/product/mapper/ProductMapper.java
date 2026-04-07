package com.example.product_test.product.mapper;

import com.example.product_test.product.model.Product;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper {

    @Select("SELECT id, product_name, price, status, created_at, updated_at FROM products WHERE id = #{id}")
    Product findById(Long id);

    @Select("SELECT id, product_name, price, status, created_at, updated_at FROM products WHERE product_name LIKE CONCAT('%', #{keyword}, '%') ORDER BY id DESC")
    List<Product> searchByKeyword(String keyword);

    @Insert("INSERT INTO products(product_name, price, status) VALUES(#{productName}, #{price}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);
}
