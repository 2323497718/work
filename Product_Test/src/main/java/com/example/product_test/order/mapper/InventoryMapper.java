package com.example.product_test.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InventoryMapper {

    @Select("SELECT available_stock FROM inventory WHERE product_id = #{productId}")
    Integer findAvailableStock(Long productId);

    @Update("UPDATE inventory SET available_stock = available_stock - 1, locked_stock = locked_stock + 1, version = version + 1 WHERE product_id = #{productId} AND available_stock > 0")
    int reserveStock(Long productId);

    @Update("UPDATE inventory SET locked_stock = locked_stock - 1, version = version + 1 WHERE product_id = #{productId} AND locked_stock > 0")
    int confirmPaid(Long productId);

    @Update("UPDATE inventory SET available_stock = available_stock + 1, locked_stock = locked_stock - 1, version = version + 1 WHERE product_id = #{productId} AND locked_stock > 0")
    int releaseLocked(Long productId);
}
