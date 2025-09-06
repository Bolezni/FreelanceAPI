package com.bolezni.service;

import com.bolezni.dto.*;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

    void register(RegisterRequest registerRequest);

    void sendTokenForResetPassword(String email);

    void resetPassword(ResetPasswordDto resetPasswordDto);

    LoginResponse refreshJwtToken(RefreshTokenDto refreshTokenDto);
}