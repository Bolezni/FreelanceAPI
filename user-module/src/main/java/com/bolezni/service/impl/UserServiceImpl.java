package com.bolezni.service.impl;

import com.bolezni.dto.UserResponseDto;
import com.bolezni.dto.UserUpdateDto;
import com.bolezni.mapper.UserMapper;
import com.bolezni.model.UserEntity;
import com.bolezni.repository.UserRepository;
import com.bolezni.security.CustomUserDetails;
import com.bolezni.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto getAuthenticationUser() {
        UserEntity user = getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userMapper.userToUserResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateUserInfo(String id, UserUpdateDto userUpdateDto) {
        if (userUpdateDto == null) {
            log.error("UserUpdateDto is null");
            throw new RuntimeException("UserUpdateDto is null");
        }

        UserEntity currentUser = getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!Objects.equals(id, currentUser.getId())) {
            log.error("User id mismatch");
            throw new RuntimeException("User id mismatch");
        }

        boolean isChanged = updateUserInfo(currentUser, userUpdateDto);

        if (!isChanged) {
            log.info("Project has changes");
            return userMapper.userToUserResponseDto(currentUser);
        }

        UserEntity savedUser = userRepository.save(currentUser);
        log.info("User has been saved with id {}", savedUser.getId());
        return userMapper.userToUserResponseDto(savedUser);
    }

    private boolean updateUserInfo(UserEntity user, UserUpdateDto userUpdateDto) {
        boolean hasChanged = false;

        hasChanged |= updateStringField(userUpdateDto.firstName(), user.getFirstName(), user::setFirstName);
        hasChanged |= updateStringField(userUpdateDto.lastName(), user.getLastName(), user::setLastName);

        return hasChanged;
    }

    private boolean updateStringField(String newValue, String currentValue, Consumer<String> setter) {
        if (StringUtils.hasText(newValue)) {
            String trimmedValue = newValue.trim();
            if (!Objects.equals(trimmedValue, currentValue)) {
                setter.accept(trimmedValue);
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public void deleteUserById(String id) {
        if (StringUtils.hasText(id)) {
            UserEntity currentUser = getCurrentUser().orElseThrow(() -> new RuntimeException("User not found"));
            if (!Objects.equals(id, currentUser.getId())) {
                log.error("User id mismatch");
                throw new RuntimeException("User id mismatch");
            }
            userRepository.deleteById(id);
        } else {
            log.error("User id is null");
            throw new RuntimeException("User id is null");
        }
    }

    private Optional<UserEntity> getCurrentUser() {
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
}
