package com.bolezni.dto;

import com.bolezni.model.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        String id,
        String title,
        String body,
        NotificationType type,
        boolean isRead,
        LocalDateTime sentAt,
        String clickAction
) {
}
