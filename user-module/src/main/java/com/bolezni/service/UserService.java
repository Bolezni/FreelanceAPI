package com.bolezni.service;

import com.bolezni.dto.ChangePasswordDto;
import com.bolezni.dto.UserResponseDto;
import com.bolezni.dto.UserUpdateDto;

public interface UserService {
    UserResponseDto getUserById(String id);

    UserResponseDto getAuthenticationUser();

    UserResponseDto updateUserInfo(String id, UserUpdateDto userUpdateDto);

    void deleteUserById(String id);

    void changePassword(ChangePasswordDto passwordDto);
}
