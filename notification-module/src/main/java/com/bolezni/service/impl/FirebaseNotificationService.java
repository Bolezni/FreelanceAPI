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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseNotificationService implements NotificationService {

    private final FirebaseMessaging firebaseMessaging;

    private final UserDeviceTokenRepository deviceTokenRepository;

    private final NotificationRepository notificationRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public NotificationResponse sendNotification(String title,
                                                 String body,
                                                 Map<String, String> data,
                                                 NotificationType type) {

        UserEntity user = UserUtils.getCurrentUser().orElseThrow(() -> new RuntimeException("User not logged in"));

        if (!user.isPushNotificationsEnabled()) {
            log.warn("User is not push notifications enabled");
            throw new RuntimeException("User is not push notifications enabled");
        }

        NotificationEntity savedNotification = sendNotificationToUser(user, title, body, data, type);

        return new NotificationResponse(
                savedNotification.getId(),
                savedNotification.getTitle(),
                savedNotification.getBody(),
                savedNotification.getType(),
                savedNotification.isRead(),
                savedNotification.getSentAt(), data != null ? data.get("clickAction") : null);
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

    private boolean isTokenInvalid(Exception error) {
        return error.getMessage() != null && (error.getMessage()
                .contains("registration-token-not-registered") ||
                error.getMessage().contains("invalid-registration-token"));
    }

    @Override
    public void sendToToken(String token,
                            String title,
                            String body, Map<String, String> data,
                            String userName) throws FirebaseMessagingException {

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
    @Transactional
    public NotificationResponse sendToProjectMembers(Long projectId,
                                                     String title,
                                                     String body,
                                                     Map<String, String> data,
                                                     NotificationType type) {

        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (project.getAuthor() == null) {
            log.error("Project author not found");
            throw new RuntimeException("Project author not found");
        }

        UserEntity freelancer = project.getFreelancer();

        if (freelancer == null) {
            log.error("Freelancer not found");
            throw new RuntimeException("Freelancer not found");
        }

        if (!freelancer.isPushNotificationsEnabled()) {
            log.warn("Freelancer is not push notifications enabled");
            throw new RuntimeException("Freelancer is not push notifications enabled");
        }

        NotificationEntity savedNotification = sendNotificationToUser(freelancer, title, body, data, type);

        return new NotificationResponse(
                savedNotification.getId(),
                savedNotification.getTitle(),
                savedNotification.getBody(),
                savedNotification.getType(),
                savedNotification.isRead(),
                savedNotification.getSentAt(), data != null ? data.get("clickAction") : null);
    }

    private NotificationEntity sendNotificationToUser(UserEntity user,
                                                      String title,
                                                      String body,
                                                      Map<String, String> data,
                                                      NotificationType type) {

        NotificationEntity notification = createNotification(user, title, body, data, type);
        Set<UserDeviceTokenEntity> deactivatedTokens = new HashSet<>();

        try {
            List<UserDeviceTokenEntity> activeTokens = getActiveTokens(user);
            notification = notificationRepository.save(notification);

            boolean anySuccess = sendNotificationForAllUserDevice(activeTokens, title, body, data, user.getFullName(), deactivatedTokens);
            notification.setDeliveryStatus(anySuccess ? DeliveryStatus.SENT : DeliveryStatus.FAILED);


            if(!deactivatedTokens.isEmpty()) {
                deviceTokenRepository.saveAll(deactivatedTokens);
                log.info("Deactivated {} invalid tokens for user {}", deactivatedTokens.size(), user.getId());

            }

            log.info("Notification {} for user {}: {}",
                    notification.getId(), user.getId(),
                    anySuccess ? "SENT" : "FAILED");
        } catch (Exception e) {
            log.error("Failed to send notification to user {}: {}",
                    user.getId(), e.getMessage());

            notification.setDeliveryStatus(DeliveryStatus.FAILED);
        }

        return notificationRepository.save(notification);
    }

    private List<UserDeviceTokenEntity> getActiveTokens(UserEntity user) {
        List<UserDeviceTokenEntity> activeTokens = user.getDeviceTokens()
                .stream()
                .filter(UserDeviceTokenEntity::isActive)
                .toList();

        if (activeTokens.isEmpty()) {
            log.warn("No active device tokens found for user {}", user.getId());
            throw new RuntimeException("No active device tokens found");
        }

        return activeTokens;
    }

    private NotificationEntity createNotification(UserEntity user,
                                                  String title, String body,
                                                  Map<String, String> data,
                                                  NotificationType type) {
        return NotificationEntity.builder()
                .user(user)
                .title(title)
                .body(body)
                .data(data != null ? convertMapToJson(data) : "{}")
                .deliveryStatus(DeliveryStatus.PENDING)
                .type(type)
                .build();
    }

    private boolean sendNotificationForAllUserDevice(List<UserDeviceTokenEntity> activeTokens,
                                                     String title,
                                                     String body,
                                                     Map<String, String> data,
                                                     String username,
                                                     Set<UserDeviceTokenEntity> deactivatedTokens) {

        if (activeTokens.isEmpty()) {
            log.error("No active tokens found");
            throw new RuntimeException("No active tokens found");
        }
        boolean anySuccess = false;
        int successCount = 0;
        int failureCount = 0;

        for (UserDeviceTokenEntity token : activeTokens) {
            try {
                sendToToken(token.getDeviceToken(), title, body, data, username);
                successCount++;
                anySuccess = true;
            } catch (FirebaseMessagingException e) {
                failureCount++;
                log.error("Firebase error for token ...{}: {} (code: {})",
                        getTokenSuffix(token.getDeviceToken()), e.getMessage(), e.getMessagingErrorCode());

                handleFailedTokenAsync(token, e,deactivatedTokens);

            } catch (Exception e) {
                failureCount++;
                log.error("Unexpected error for token ...{}: {}",
                        getTokenSuffix(token.getDeviceToken()), e.getMessage());
            }
        }
        log.info("Notification batch completed: {} success, {} failed", successCount, failureCount);
        return anySuccess;
    }


    public void handleFailedTokenAsync(UserDeviceTokenEntity tokenEntity, Exception error, Set<UserDeviceTokenEntity> deactivatedTokens) {
        try {
            if (isTokenInvalid(error)) {
                tokenEntity.setActive(false);
                deactivatedTokens.add(tokenEntity);
                log.info("Deactivated invalid token ending ...{}", getTokenSuffix(tokenEntity.getDeviceToken()));
            }
        } catch (Exception e) {
            log.error("Failed to handle invalid token: {}", e.getMessage());
        }
    }

    private String getTokenSuffix(String token) {
        if (token == null || token.length() < 4) {
            return "****";
        }
        return token.substring(token.length() - 4);
    }

}
