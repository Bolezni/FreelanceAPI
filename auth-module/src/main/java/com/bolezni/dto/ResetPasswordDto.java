package com.bolezni.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordDto(
        @NotBlank String token,
        @NotBlank @Min(6) String newPassword
) {
}
