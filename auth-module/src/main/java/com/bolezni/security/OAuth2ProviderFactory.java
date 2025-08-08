package com.bolezni.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuth2ProviderFactory {

    private final List<OAuth2Provider> OAuth2Providers;

    public OAuth2Provider getAuthProvider(String providerId) {
        return OAuth2Providers.stream()
                .filter(provider -> provider.supports(providerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported provider: " + providerId));
    }
}
