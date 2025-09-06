package com.bolezni.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordDto(
        @NotBlank String oldPassword,
        @NotBlank @Min(6) String newPassword
) {
}
