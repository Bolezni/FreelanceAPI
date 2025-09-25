package com.bolezni.service.impl;

import com.bolezni.dto.*;
import com.bolezni.events.ResetPasswordEvent;
import com.bolezni.model.PasswordResetTokenEntity;
import com.bolezni.model.Roles;
import com.bolezni.model.UserEntity;
import com.bolezni.repository.PasswordResetTokenRepository;
import com.bolezni.repository.UserRepository;
import com.bolezni.security.CustomUserDetails;
import com.bolezni.security.jwt.JwtService;
import com.bolezni.service.AuthService;
import com.bolezni.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final VerificationService emailVerificationService;
    private final UserDetailsService userDetailsService;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordResetTokenRepository resetTokenRepository;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        if (loginRequest == null) {
            throw new IllegalArgumentException("loginRequest cannot be null");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(),
                        loginRequest.password()));

        log.info("Authenticated user: {} with roles {}", authentication.getName(), authentication.getAuthorities());

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String jwtToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        UserEntity user = userDetails.getUser();

        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                jwtToken,
                refreshToken
        );
    }

    @Override
    @Transactional
    public void register(RegisterRequest registerRequest) {

        if (registerRequest == null) {
            throw new IllegalArgumentException("RegisterRequest is null");
        }

        String username = registerRequest.username();
        String email = registerRequest.email();

        if (userRepository.existsByUsernameOrEmail(username, email)) {
            throw new IllegalArgumentException("Username or email already exists");
        }

        UserEntity userEntity = createNewUser(registerRequest, registerRequest.roles());

        UserEntity savedUser = userRepository.save(userEntity);

        emailVerificationService.createVerificationToken(savedUser);
    }

    @Override
    @Transactional
    public void sendTokenForResetPassword(String email) {
        if(!StringUtils.hasText(email)){
            log.error("Email is empty");
            throw new IllegalArgumentException("Email is empty");
        }

        UserEntity findUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = UUID.randomUUID().toString();

        PasswordResetTokenEntity resetTokenEntity = PasswordResetTokenEntity.builder()
                .token(token)
                .user(findUser)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();

        resetTokenRepository.save(resetTokenEntity);

        eventPublisher.publishEvent(new ResetPasswordEvent(
                this,
                findUser.getEmail(),
                token
        ));
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordDto resetPasswordDto) {
        if(resetPasswordDto == null){
            log.error("resetPasswordDto is null");
            throw new IllegalArgumentException("resetPasswordDto is null");
        }

        PasswordResetTokenEntity resetTokenEntity = resetTokenRepository.findByToken(resetPasswordDto.token())
                .orElseThrow(() -> new IllegalArgumentException("Token not found"));

        String newPassword = resetPasswordDto.newPassword();

        UserEntity user = resetTokenEntity.getUser();

        if(resetTokenEntity.getExpiryDate().isBefore(LocalDateTime.now())){
            log.error("Token is expired");
            throw new IllegalArgumentException("Token is expired");
        }

        if(passwordEncoder.matches(newPassword, user.getPassword())){
            log.error("New password must be different from current password");
            throw new IllegalArgumentException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        resetTokenRepository.delete(resetTokenEntity);
    }

    @Override
    public LoginResponse refreshJwtToken(RefreshTokenDto refreshTokenDto) {
        if(refreshTokenDto == null){
            log.error("refreshTokenDto is null");
            throw new IllegalArgumentException("refreshTokenDto is null");
        }

        String refreshToken = refreshTokenDto.refreshToken();

        String username = jwtService.extractUsername(refreshToken);
        if (username == null || username.trim().isEmpty()) {
            log.warn("Username extracted from refresh token is null or empty");
            throw new IllegalArgumentException("Invalid refresh token: cannot extract username");
        }
        CustomUserDetails userDetails;
        try{
            userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);
        }catch (Exception e){
            log.warn("Couldn't upload user data for username: {}", username);
            throw new IllegalArgumentException("Invalid refresh token: user not found");

        }

        if (!jwtService.isValidToken(refreshToken, userDetails)) {
            log.warn("Invalid refresh token for user: {}", username);
            throw new IllegalArgumentException("Refresh token is invalid or expired");
        }

        String newJwtToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        return new LoginResponse(
                userDetails.getUser().getId(),
                userDetails.getUser().getUsername(),
                userDetails.getUser().getEmail(),
                newJwtToken,
                newRefreshToken

        );
    }

    private UserEntity createNewUser(RegisterRequest registerRequest, Set<String> roles) {
        return UserEntity.builder()
                .firstName(registerRequest.firstname())
                .lastName(registerRequest.lastname())
                .username(registerRequest.username())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .roles(processRoles(roles))
                .build();
    }

    private Set<Roles> processRoles(Set<String> roleStrings) {
        Set<Roles> roles = roleStrings != null ? roleStrings.stream()
                .map(String::toUpperCase)
                .map(Roles::valueOf)
                .collect(Collectors.toSet()) : new HashSet<>();

        if (roles.isEmpty()) {
            roles.add(Roles.CLIENT);
        }

        return roles;
    }
}