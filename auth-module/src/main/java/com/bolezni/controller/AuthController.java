package com.bolezni.controller;

import com.bolezni.dto.ApiResponse;
import com.bolezni.dto.LoginRequest;
import com.bolezni.dto.LoginResponse;
import com.bolezni.dto.RegisterRequest;
import com.bolezni.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    private static final String LOGIN = "/login";

    @PostMapping(LOGIN)
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest loginRequest,
                                                            HttpServletRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(loginRequest, request, response);
        ApiResponse<LoginResponse> apiResponse = new ApiResponse<>(true, loginResponse, "Successful login");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody @Valid RegisterRequest registerRequest) {
        authService.register(registerRequest);
        ApiResponse<Void> apiResponse = new ApiResponse<>(true, null, "Successful registration");
        return ResponseEntity.ok(apiResponse);
    }


    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        ApiResponse<Void> apiResponse = new ApiResponse<>(true, null, "Successful logout");
        return ResponseEntity.ok(apiResponse);
    }
}
