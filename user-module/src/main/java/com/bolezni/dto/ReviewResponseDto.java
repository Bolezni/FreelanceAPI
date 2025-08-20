package com.bolezni.dto;

public record ReviewResponseDto(
        Long id,
        String reviewerId,
        String reviewedUserId,
        Integer rating,
        String comment,
        String status
) {
}
