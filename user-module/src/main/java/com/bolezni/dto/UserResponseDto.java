package com.bolezni.dto;

public record UserResponseDto(
        String id,
        String firstName,
        String lastName,
        String username,
        String email,
        boolean isVerified,
        String provider,
        String providerId
        ) {
}
