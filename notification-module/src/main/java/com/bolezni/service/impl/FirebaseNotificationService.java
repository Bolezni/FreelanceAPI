package com.bolezni.service.impl;


import com.bolezni.dto.NotificationResponse;
import com.bolezni.model.*;
import com.bolezni.repository.NotificationRepository;
import com.bolezni.repository.ProjectRepository;
import com.bolezni.repository.UserDeviceTokenRepository;
import com.bolezni.service.NotificationService;
import com.bolezni.utils.UserUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseNotificationService implements NotificationService {

    private final FirebaseMessaging firebaseMessaging;

    private final UserDeviceTokenRepository deviceTokenRepository;

    private final NotificationRepository notificationRepository;


    @Override
    @Transactional
    public NotificationResponse sendNotification(String title, String body, Map<String, String> data, NotificationType type) {
        UserEntity user = UserUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not logged in"));

        if (!user.isPushNotificationsEnabled()) {
            log.warn("User is not push notifications enabled");
            throw new RuntimeException("User is not push notifications enabled");
        }

        List<UserDeviceTokenEntity> activeTokens = user.getDeviceTokens().stream()
                .filter(UserDeviceTokenEntity::isActive)
                .toList();

        if (activeTokens.isEmpty()) {
            log.warn("No active device tokens found for user: {}", user.getId());
            throw new RuntimeException("No active device tokens found for user: " + user.getId());
        }

        NotificationEntity notification = NotificationEntity.builder()
                .user(user)
                .title(title)
                .body(body)
                .data(data != null ? convertMapToJson(data) : "{}")
                .type(type)
                .deliveryStatus(DeliveryStatus.PENDING)
                .build();

        notification = notificationRepository.saveAndFlush(notification);
        boolean anySuccess = false;

        for (UserDeviceTokenEntity token : activeTokens) {
            try {
                sendToToken(token.getDeviceToken(), title, body, data, user.getFullName());
                anySuccess = true;
            } catch (Exception e) {
                log.error("Failed to send notification to token: {}", token.getDeviceToken(), e);
                handleFailedToken(token, e);
            }
        }
        notification.setDeliveryStatus(anySuccess ? DeliveryStatus.SENT : DeliveryStatus.FAILED);

        NotificationEntity savedNotification = notificationRepository.save(notification);

        return new NotificationResponse(
                savedNotification.getId(),
                savedNotification.getTitle(),
                savedNotification.getBody(),
                savedNotification.getType(),
                savedNotification.isRead(),
                savedNotification.getSentAt(),
                data != null ? data.get("clickAction") : null);
    }

    private String convertMapToJson(Map<String, String> map) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("Error converting map to JSON", e);
            return "{}";
        }
    }

    private void handleFailedToken(UserDeviceTokenEntity tokenEntity, Exception error) {
        if (isTokenInvalid(error)) {
            tokenEntity.setActive(false);
            deviceTokenRepository.save(tokenEntity);
            log.info("Deactivated invalid token: {}", tokenEntity.getDeviceToken());
        }
    }

    private boolean isTokenInvalid(Exception error) {
        return error.getMessage() != null &&
                (error.getMessage().contains("registration-token-not-registered") ||
                        error.getMessage().contains("invalid-registration-token"));
    }

    @Override
    public void sendToToken(String token, String title, String body, Map<String, String> data, String userName) throws FirebaseMessagingException {
        String personalizedBody = body.replace("{userName}", userName);

        Message.Builder messageBuilder = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(personalizedBody)
                        .build());

        if (data != null && !data.isEmpty()) {
            messageBuilder.putAllData(data);
        }

        messageBuilder.setWebpushConfig(WebpushConfig.builder()
                .setNotification(WebpushNotification.builder()
                        .setTitle(title)
                        .setBody(personalizedBody)
                        .setIcon("/icon-192x192.png")
                        .setBadge("/badge-72x72.png")
                        .setTag("user-notification")
                        .setRequireInteraction(false)
                        .build())
                .build());

        Message message = messageBuilder.build();
        String response = firebaseMessaging.send(message);
        log.info("Successfully sent message: {}", response);
    }

    @Override
    public void sendToProjectMembers(Long projectId, String title, String body, Map<String, String> data) {

    }
}
