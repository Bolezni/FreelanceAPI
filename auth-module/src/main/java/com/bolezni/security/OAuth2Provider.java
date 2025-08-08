package com.bolezni.security;

import com.bolezni.model.UserEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2Provider {
    String getProviderName();
    UserEntity extractUser(OAuth2User oauth2User);
    boolean supports(String providerId);
}
