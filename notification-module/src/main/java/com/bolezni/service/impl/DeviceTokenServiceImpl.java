package com.bolezni.service.impl;

import com.bolezni.dto.DeviceTokenDto;
import com.bolezni.dto.TokenRegistrationRequest;
import com.bolezni.model.UserDeviceTokenEntity;
import com.bolezni.model.UserEntity;
import com.bolezni.repository.UserDeviceTokenRepository;
import com.bolezni.service.DeviceTokenService;
import com.bolezni.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenServiceImpl implements DeviceTokenService {

    private final UserDeviceTokenRepository deviceTokenRepository;

    @Override
    @Transactional
    public DeviceTokenDto saveOrUpdateDeviceToken(TokenRegistrationRequest registrationRequest) {
        if(registrationRequest == null) {
            log.error("registrationRequest is null");
            throw new IllegalArgumentException("registrationRequest cannot be null");
        }

        UserEntity currentUser = UserUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not logged in"));

        UserDeviceTokenEntity deviceTokenEntity = deviceTokenRepository.findByDeviceToken(registrationRequest.token())
                .orElse(UserDeviceTokenEntity.builder()
                        .user(currentUser)
                        .deviceToken(registrationRequest.token())
                        .deviceType(registrationRequest.deviceType())
                        .deviceInfo(registrationRequest.deviceInfo())
                        .isActive(true)
                        .build());

        UserDeviceTokenEntity savedToken = deviceTokenRepository.save(deviceTokenEntity);

        return new DeviceTokenDto("success", savedToken.getId(), "Token registered successfully");
    }

    @Override
    @Transactional
    public void deleteDeviceToken(String userId, String deviceToken) {
        UserEntity currentUser = UserUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not logged in"));

        UserUtils.isCurrentUser(userId, currentUser.getId());

        deviceTokenRepository.findByDeviceToken(deviceToken)
                .ifPresent(deviceTokenRepository::delete);
    }

    @Override
    @Transactional
    public void deleteAllDeviceTokens(String userId) {
        UserEntity currentUser = UserUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not logged in"));

        UserUtils.isCurrentUser(userId, currentUser.getId());

        List<UserDeviceTokenEntity> userTokens = currentUser.getDeviceTokens();
        userTokens.forEach(token -> token.setActive(false));
        deviceTokenRepository.saveAll(userTokens);
    }

    @Override
    public List<UserDeviceTokenEntity> getActiveTokensForUser(String userId) {
        UserEntity currentUser = UserUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not logged in"));

        UserUtils.isCurrentUser(userId, currentUser.getId());
        return currentUser.getDeviceTokens().stream()
                .filter(UserDeviceTokenEntity::isActive)
                .collect(Collectors.toList());
    }
}
