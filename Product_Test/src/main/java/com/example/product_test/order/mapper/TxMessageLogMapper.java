package com.example.product_test.order.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TxMessageLogMapper {

    @Insert("INSERT IGNORE INTO tx_message_log(msg_key, topic, consumed) VALUES(#{msgKey}, #{topic}, 1)")
    int insertIgnore(@Param("msgKey") String msgKey, @Param("topic") String topic);
}
