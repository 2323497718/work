package com.example.product_test.user.mapper;

import com.example.product_test.user.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("SELECT id, username, password_hash, email, created_at, updated_at FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Insert("INSERT INTO users(username, password_hash, email) VALUES(#{username}, #{passwordHash}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
}
