package com.bolezni.dto;

import com.bolezni.model.DeviceType;
import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.NonNull;

public record TokenRegistrationRequest(
        @NotBlank String token,
        @NonNull DeviceType deviceType,
        @NotBlank String deviceInfo
) {
}
