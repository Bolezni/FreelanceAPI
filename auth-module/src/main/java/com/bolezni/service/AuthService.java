package com.bolezni.service;

import com.bolezni.dto.LoginRequest;
import com.bolezni.dto.LoginResponse;
import com.bolezni.dto.RegisterRequest;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

    void register(RegisterRequest registerRequest);
}