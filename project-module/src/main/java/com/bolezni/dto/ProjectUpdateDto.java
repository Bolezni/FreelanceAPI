package com.bolezni.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Set;

public record ProjectUpdateDto(
        @NotNull String title,
        @NotNull String description,
        @Positive(message = "Amount must be positive") @NotNull BigDecimal price,
        @NotNull Set<String> categories
) {
}
