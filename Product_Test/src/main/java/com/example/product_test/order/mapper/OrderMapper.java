package com.example.product_test.order.mapper;

import com.example.product_test.order.model.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO orders(order_no, user_id, product_id, quantity, amount, order_status) VALUES(#{orderNo}, #{userId}, #{productId}, #{quantity}, #{amount}, #{orderStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Select("SELECT id, order_no, user_id, product_id, quantity, amount, order_status, created_at, updated_at FROM orders WHERE order_no = #{orderNo}")
    Order findByOrderNo(String orderNo);

    @Select("SELECT id, order_no, user_id, product_id, quantity, amount, order_status, created_at, updated_at FROM orders WHERE user_id = #{userId} AND product_id = #{productId} LIMIT 1")
    Order findByUserAndProduct(Long userId, Long productId);

    @Update("UPDATE orders SET order_status = #{toStatus} WHERE order_no = #{orderNo} AND order_status = #{fromStatus}")
    int updateStatus(@Param("orderNo") String orderNo, @Param("fromStatus") Integer fromStatus, @Param("toStatus") Integer toStatus);
}
