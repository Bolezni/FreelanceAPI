package com.bolezni.service;

import com.bolezni.dto.LoginRequest;
import com.bolezni.dto.LoginResponse;
import com.bolezni.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest, HttpServletRequest httpServletRequest, HttpServletResponse response);

    void register(RegisterRequest registerRequest);

    void logout(HttpServletRequest request,HttpServletResponse response);
}