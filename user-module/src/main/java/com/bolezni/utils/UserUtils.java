package com.bolezni.utils;

import com.bolezni.model.UserEntity;
import com.bolezni.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@Slf4j
public final class UserUtils {
    private UserUtils() {}

    public static Optional<UserEntity> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return Optional.of(((CustomUserDetails) principal).getUser());
            } else if (principal instanceof UserEntity) {
                return Optional.of((UserEntity) principal);
            }
        }
        return Optional.empty();
    }

    public static void isCurrentUser(String userId, String currentUserId) {
        if(!userId.equals(currentUserId)) {
            log.warn("User is miss math");
            throw new RuntimeException("User is miss math");
        }
    }
}
