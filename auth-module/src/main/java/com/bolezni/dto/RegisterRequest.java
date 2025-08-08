package com.bolezni.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record RegisterRequest(
        @NotBlank
        String firstname,
        @NotBlank
        String lastname,
        @NotBlank
        String email,
        @NotBlank
        String username,
        @NotBlank
        String password,
        @NotNull
        Set<String> roles
) {
}
