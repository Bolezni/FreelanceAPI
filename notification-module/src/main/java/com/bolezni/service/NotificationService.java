package com.bolezni.service;

import com.bolezni.dto.NotificationResponse;
import com.bolezni.model.NotificationType;
import com.google.firebase.messaging.FirebaseMessagingException;

import java.util.Map;

public interface NotificationService {
    NotificationResponse sendNotification(String title, String body,
                                          Map<String, String> data, NotificationType type);

    void sendToToken(String token, String title, String body,
                     Map<String, String> data, String userName) throws FirebaseMessagingException;

    void sendToProjectMembers(Long projectId, String title, String body,
                              Map<String, String> data);
}
