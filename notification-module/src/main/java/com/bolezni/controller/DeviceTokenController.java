package com.bolezni.controller;

import com.bolezni.dto.ApiResponse;
import com.bolezni.dto.DeviceTokenDto;
import com.bolezni.dto.TokenRegistrationRequest;
import com.bolezni.service.DeviceTokenService;
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
@RequestMapping("/api/v1/device-token")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;


    @PostMapping
    public ResponseEntity<ApiResponse<DeviceTokenDto>> createOrUpdateDeviceToken(@RequestBody @Valid TokenRegistrationRequest registrationRequest){
        DeviceTokenDto dto = deviceTokenService.saveOrUpdateDeviceToken(registrationRequest);
        ApiResponse<DeviceTokenDto> apiResponse = ApiResponse.<DeviceTokenDto>builder()
                .status(true)
                .data(dto)
                .message("Device token created")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
}
