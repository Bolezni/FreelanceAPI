package com.bolezni.dto;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Set;

public record ProjectUpdateDto(
        String title,
        String description,
        @Positive(message = "Amount must be positive") BigDecimal price,
        Set<String> categories
) {
}
