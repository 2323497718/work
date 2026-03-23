package com.example.product_test.user.service.impl;

import com.example.product_test.user.dto.AuthResponse;
import com.example.product_test.user.dto.LoginRequest;
import com.example.product_test.user.dto.RegisterRequest;
import com.example.product_test.user.mapper.UserMapper;
import com.example.product_test.user.model.User;
import com.example.product_test.user.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new IllegalArgumentException("username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userMapper.insert(user);

        return new AuthResponse(user.getId(), user.getUsername(), generateToken(user.getUsername()));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("username or password is incorrect");
        }
        return new AuthResponse(user.getId(), user.getUsername(), generateToken(user.getUsername()));
    }

    private String generateToken(String username) {
        return username + "-" + UUID.randomUUID();
    }
}
