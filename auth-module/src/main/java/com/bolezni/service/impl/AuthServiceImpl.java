package com.bolezni.service.impl;

import com.bolezni.dto.LoginRequest;
import com.bolezni.dto.LoginResponse;
import com.bolezni.dto.RegisterRequest;
import com.bolezni.model.Roles;
import com.bolezni.model.UserEntity;
import com.bolezni.repository.UserRepository;
import com.bolezni.security.CustomUserDetails;
import com.bolezni.security.jwt.JwtService;
import com.bolezni.service.AuthService;
import com.bolezni.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
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

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        if (loginRequest == null) {
            throw new IllegalArgumentException("loginRequest cannot be null");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(),
                        loginRequest.password()));

        log.info("Authenticated user: {}", authentication.getName());

        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(loginRequest.username());

        String jwtToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new LoginResponse(
                userDetails.getUser().getId(),
                userDetails.getUser().getUsername(),
                userDetails.getUser().getEmail(),
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

        Set<Roles> roles = processRoles(registerRequest.roles());

        UserEntity userEntity = createNewUser(registerRequest, roles);

        UserEntity savedUser = userRepository.save(userEntity);

        emailVerificationService.createVerificationToken(savedUser);
    }


    private UserEntity createNewUser(RegisterRequest registerRequest, Set<Roles> roles) {
        return UserEntity.builder()
                .firstName(registerRequest.firstname())
                .lastName(registerRequest.lastname())
                .username(registerRequest.username())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .roles(roles)
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