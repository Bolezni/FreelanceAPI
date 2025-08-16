package com.bolezni.dto;

public record LoginResponse(
        String id,
        String username,
        String email,
        String jwtToken,
        String refreshToken
) {
}
