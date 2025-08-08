package com.bolezni.security;

import com.bolezni.model.UserEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GitHubOAuth2Provider implements OAuth2Provider {

    @Override
    public String getProviderName() {
        return "github";
    }

    @Override
    public UserEntity extractUser(OAuth2User oauth2User) {
        Map<String,Object> attributes = oauth2User.getAttributes();
        String fullName = (String) attributes.get("name");
        String[] names = fullName != null ? fullName.split(" ") : new String[0];

        return UserEntity.builder()
                .email((String) attributes.get("email"))
                .firstName(names.length > 0 ? names[0] : null)
                .lastName(names.length > 1 ? names[1] : null)
                .provider(getProviderName())
                .providerId(String.valueOf(attributes.get("id")))
                .build();
    }

    @Override
    public boolean supports(String providerId) {
        return false;
    }
}
