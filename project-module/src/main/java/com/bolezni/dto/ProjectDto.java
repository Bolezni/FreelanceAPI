package com.bolezni.dto;

import java.math.BigDecimal;
import java.util.Set;

public record ProjectDto(
        Long id,
        String title,
        String description,
        BigDecimal price,
        String authorId,
        Set<String> categories
) {
}
