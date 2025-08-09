package com.bolezni.service.impl;

import com.bolezni.dto.VerifyEmailRequest;
import com.bolezni.events.UserRegisteredEvent;
import com.bolezni.model.EmailVerificationToken;
import com.bolezni.model.UserEntity;
import com.bolezni.repository.EmailVerificationTokenRepository;
import com.bolezni.repository.UserRepository;
import com.bolezni.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService implements VerificationService {
    private final EmailVerificationTokenRepository emailTokenRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.verification.expiration-minutes:15}")
    private int expirationMinutes;

    @Override
    @Transactional
    public void verifyByCode(VerifyEmailRequest request) {
        Objects.requireNonNull(request, "Request cannot be null");

        if (request.getVerificationCode() == null || request.getVerificationCode().isEmpty()) {
            log.error("Invalid verification code");
            throw new IllegalArgumentException("Verification code cannot be null or empty");
        }


        EmailVerificationToken token = emailTokenRepository.findByVerificationCodeAndEmail(request.getVerificationCode(), request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Code not found"));

        UserEntity user = token.getUser();

        checkUserVerification(user, token);

        token.setIsUsed(true);
        user.setVerified(true);

        emailTokenRepository.save(token);
        userRepository.save(user);

        emailTokenRepository.deleteByEmail(user.getEmail());

        log.info("Email verified successfully for: {}", request.getEmail());
    }

    @Override
    @Transactional
    public void verifyByCode(String tokenValue) {
        Objects.requireNonNull(tokenValue, "Token cannot be null");

        EmailVerificationToken token = emailTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        UserEntity user = token.getUser();

        checkUserVerification(user, token);

        token.setIsUsed(true);
        user.setVerified(true);

        emailTokenRepository.save(token);
        userRepository.save(user);

        emailTokenRepository.deleteByEmail(user.getEmail());

        log.info("Email verified successfully for: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void createVerificationToken(UserEntity user) {
        emailTokenRepository.deleteByEmail(user.getEmail());

        String token = generateToken();
        String verificationCode = generateVerificationCode();

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .email(user.getEmail())
                .verificationCode(verificationCode)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .isUsed(false)
                .build();

        var savedEmailToken = emailTokenRepository.save(verificationToken);

        publishEvent(savedEmailToken);
        log.info("Created verification token for email: {}", user.getEmail());

    }

    @Override
    @Transactional
    public void resendVerificationToken(String email) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with email: " + email);
        }

        UserEntity user = userOpt.get();

        if (user.isVerified()) {
            throw new IllegalArgumentException("Email is already verified");
        }

        Optional<EmailVerificationToken> existingToken = emailTokenRepository.findByEmailAndIsUsedFalse(email);

        if (existingToken.isPresent() && !existingToken.get().isExpired()) {
            String code = existingToken.get().getVerificationCode();


            publishEvent(existingToken.get(), code);

            log.info("Resent existing verification code for email: {}", email);
        } else {
            createVerificationToken(user);

            log.info("Created and sent new verification code for email: {}", email);
        }
    }

//    @Scheduled(fixedRate = 3600000) // каждый час
//    @Transactional
//    public void cleanupExpiredTokens() {
//        LocalDateTime cutoff = LocalDateTime.now();
//        emailTokenRepository.deleteByExpiresAtBefore(cutoff);
//        log.info("Cleaned up expired verification tokens before: {}", cutoff);
//    }

    private void checkUserVerification(UserEntity user, EmailVerificationToken token) {
        if (user.isVerified()) {
            log.info("User already verified");
            return;
        }

        if (token.isExpired()) {
            log.warn("Verification failed: Expired token for email {}", user.getEmail());
            emailTokenRepository.delete(token);
            throw new IllegalArgumentException("Verification token has expired");
        }

        if (token.getIsUsed()) {
            log.warn("Verification failed: Token already used for email {}", user.getEmail());
            throw new IllegalArgumentException("Verification token has already been used");
        }
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6-значный код
        return String.valueOf(code);
    }

    private void publishEvent(EmailVerificationToken verificationToken) {
        eventPublisher.publishEvent(new UserRegisteredEvent(
                this,
                verificationToken.getEmail(),
                verificationToken.getUser().getFirstName(),
                verificationToken.getToken(),
                verificationToken.getVerificationCode()

        ));
    }

    private void publishEvent(EmailVerificationToken verificationToken, String verificationCode) {
        eventPublisher.publishEvent(new UserRegisteredEvent(
                this,
                verificationToken.getEmail(),
                verificationToken.getUser().getFirstName(),
                verificationToken.getToken(),
                verificationCode

        ));
    }
}
