package com.bolezni.service;

import com.bolezni.dto.VerifyEmailRequest;
import com.bolezni.model.UserEntity;

public interface VerificationService {
    void verifyByCode(VerifyEmailRequest request);

    void verifyByCode(String token);

    void createVerificationToken(UserEntity user);

    void resendVerificationToken(String email);
}
