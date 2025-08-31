package com.bolezni.dto;

import com.bolezni.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.NonNull;

import java.util.Map;

public record NotificationRequest(
        @NotBlank String title,
        @NotBlank String body,
        @NonNull Map<String,String> data,
        @NonNull NotificationType type
) {


}
