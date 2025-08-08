package com.bolezni.security;

import com.bolezni.model.UserEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GoogleOAuth2Provider implements OAuth2Provider {

    @Override
    public String getProviderName() {
        return "google";
    }

    @Override
    public UserEntity extractUser(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        return UserEntity.builder()
                .email((String) attributes.get("email"))
                .firstName((String) attributes.get("given_name"))
                .lastName((String) attributes.get("family_name"))
                .provider(getProviderName())
                .providerId((String)attributes.get("sub"))
                .build();
    }

    @Override
    public boolean supports(String providerId) {
        return getProviderName().equals(providerId);
    }
}
