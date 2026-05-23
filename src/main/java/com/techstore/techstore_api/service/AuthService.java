package com.techstore.techstore_api.service;

import com.techstore.techstore_api.dto.request.LoginRequest;
import com.techstore.techstore_api.dto.request.RegisterRequest;
import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.dto.response.AuthResponse;
import com.techstore.techstore_api.dto.response.UserResponse;

// src/main/java/com/techstore/techstore_api/service/AuthService.java
public interface AuthService {
    // On renvoie AuthResponse (User + Token) au lieu de UserResponse
    ApiResponse<AuthResponse> registerUser(RegisterRequest registerRequest);
    ApiResponse<AuthResponse> loginUser(LoginRequest loginRequest);
}