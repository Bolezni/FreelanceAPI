package com.bolezni.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.NonNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record ProjectCreateDto(
        @NotBlank String title,
        @NotBlank String description,
        @NonNull @Positive BigDecimal price,
        @NonNull Set<String> categories,
        @NonNull LocalDateTime deadline
) {
}
