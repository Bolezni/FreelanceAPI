package com.bolezni.service;

import com.bolezni.dto.DeviceTokenDto;
import com.bolezni.dto.TokenRegistrationRequest;
import com.bolezni.model.UserDeviceTokenEntity;

import java.util.List;

public interface DeviceTokenService {
    DeviceTokenDto saveOrUpdateDeviceToken(TokenRegistrationRequest registrationRequest);

    void deleteDeviceToken(String userId, String deviceToken);

    void deleteAllDeviceTokens(String userId);

    List<UserDeviceTokenEntity> getActiveTokensForUser(String userId);
}
