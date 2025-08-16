package com.bolezni.controller;

import com.bolezni.dto.*;
import com.bolezni.service.AuthService;
import com.bolezni.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    private final VerificationService verificationService;

    private static final String LOGIN = "/login";

    @PostMapping(LOGIN)
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        ApiResponse<LoginResponse> apiResponse = new ApiResponse<>(true, loginResponse, "Successful login");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody @Valid RegisterRequest registerRequest) {
        authService.register(registerRequest);
        ApiResponse<Void> apiResponse = new ApiResponse<>(true, null, "Successful registration");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyCode(@RequestBody @Valid VerifyEmailRequest request) {
        verificationService.verifyByCode(request);
        ApiResponse<Void> apiResponse = new ApiResponse<>(true, null, "Successful verification");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyCodeByParam(@RequestParam(name = "token") String token) {
        verificationService.verifyByCode(token);
        ApiResponse<Void> apiResponse = new ApiResponse<>(true, null, "Successful verification");
        return ResponseEntity.ok(apiResponse);
    }
}
