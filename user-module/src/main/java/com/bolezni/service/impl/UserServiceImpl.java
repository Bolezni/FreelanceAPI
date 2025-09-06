package com.bolezni.service.impl;

import com.bolezni.dto.ChangePasswordDto;
import com.bolezni.dto.UserResponseDto;
import com.bolezni.dto.UserUpdateDto;
import com.bolezni.mapper.UserMapper;
import com.bolezni.model.UserEntity;
import com.bolezni.repository.UserRepository;
import com.bolezni.service.UserService;
import com.bolezni.utils.UpdateFieldUtils;
import com.bolezni.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto getAuthenticationUser() {
        UserEntity user = UserUtils.getCurrentUser()
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

        UserEntity currentUser = UserUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserUtils.isCurrentUser(id, currentUser.getId());

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
        return UpdateFieldUtils.updateMultipleFields(
                () -> UpdateFieldUtils.updateStringField(userUpdateDto.firstName(), user::getFirstName, user::setFirstName),
                () -> UpdateFieldUtils.updateStringField(userUpdateDto.lastName(), user::getLastName, user::setLastName)
        );
    }

    @Override
    @Transactional
    public void deleteUserById(String id) {
        if (StringUtils.hasText(id)) {
            UserEntity currentUser = UserUtils.getCurrentUser().orElseThrow(() -> new RuntimeException("User not found"));
            UserUtils.isCurrentUser(id, currentUser.getId());
            userRepository.deleteById(id);
        } else {
            log.error("User id is null");
            throw new RuntimeException("User id is null");
        }
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordDto resetPasswordDto) {
        if (resetPasswordDto == null) {
            log.error("resetPasswordDto is null");
            throw new IllegalArgumentException("resetPasswordDto is null");
        }

        UserEntity user = UserUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found"));

        String oldPassword = resetPasswordDto.oldPassword();
        String newPassword = resetPasswordDto.newPassword();

        if (StringUtils.hasText(oldPassword) && StringUtils.hasText(newPassword)) {
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                log.error("Current password is incorrect");
                throw new IllegalArgumentException("Current password is incorrect");
            }
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            log.info("New password must be different from current password");
            throw new IllegalArgumentException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
