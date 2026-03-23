package com.example.product_test.user.service;

import com.example.product_test.user.dto.AuthResponse;
import com.example.product_test.user.dto.LoginRequest;
import com.example.product_test.user.dto.RegisterRequest;

public interface UserService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
