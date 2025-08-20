package com.bolezni.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.lang.NonNull;

public record ReviewUpdateDto(
       @NonNull @Min(1) @Max(5) Integer rating,
       @NonNull String comment
) {
}
