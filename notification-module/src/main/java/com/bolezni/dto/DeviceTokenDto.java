package com.bolezni.dto;

public record DeviceTokenDto(
        String status,
        String tokenId,
        String message
) {
}
