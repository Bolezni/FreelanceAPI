package com.bolezni.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.lang.NonNull;

public record ReviewCreateDto(
        @NotBlank String reviewedId,
        @NonNull @Min(1) @Max(5) Integer rating,
        @NotBlank @Size(max = 1000) String comment,
        @NotBlank String status
) {
}
