package com.bolezni.controller;


import com.bolezni.dto.*;
import com.bolezni.service.DeviceTokenService;
import com.bolezni.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification")
public class NotificationController {

    private final NotificationService notificationService;

    private final DeviceTokenService deviceTokenService;

    @PostMapping("/register-token")
    public ResponseEntity<DeviceTokenDto> registerToken(@RequestBody @Valid TokenRegistrationRequest registrationRequest) {
        DeviceTokenDto dto = deviceTokenService.saveOrUpdateDeviceToken(registrationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendNotificationByToken(@RequestBody @Valid NotificationRequest registrationRequest) {
        NotificationResponse response = notificationService.sendNotification(registrationRequest.title(),registrationRequest.body(),registrationRequest.data(),registrationRequest.type());
        ApiResponse<NotificationResponse> apiResponse = ApiResponse.<NotificationResponse>builder()
                .status(true)
                .data(response)
                .message("Notification sent successfully")
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
}
