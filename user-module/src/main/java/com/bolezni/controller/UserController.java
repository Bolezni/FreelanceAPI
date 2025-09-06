package com.bolezni.controller;

import com.bolezni.dto.ApiResponse;
import com.bolezni.dto.ChangePasswordDto;
import com.bolezni.dto.UserResponseDto;
import com.bolezni.dto.UserUpdateDto;
import com.bolezni.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser() {
        UserResponseDto dto = userService.getAuthenticationUser();
        ApiResponse<UserResponseDto> apiResponse = ApiResponse.<UserResponseDto>builder()
                .status(true)
                .data(dto)
                .message("Successful get current user")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserInfo(@PathVariable(name = "id") String id,
                                                                       @RequestBody UserUpdateDto userUpdateDto) {
        UserResponseDto dto = userService.updateUserInfo(id, userUpdateDto);

        ApiResponse<UserResponseDto> apiResponse = ApiResponse.<UserResponseDto>builder()
                .status(true)
                .data(dto)
                .message("Successful update user info")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@RequestBody @Valid ChangePasswordDto changePasswordDto) {
        userService.changePassword(changePasswordDto);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .status(true)
                .message("Successful reset password")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> deleteCurrentUser(@PathVariable(name = "id") String id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}
