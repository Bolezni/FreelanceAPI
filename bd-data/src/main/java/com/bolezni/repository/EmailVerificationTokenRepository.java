package com.bolezni.repository;

import com.bolezni.model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByVerificationCodeAndEmail(String verificationCode, String email);

    Optional<EmailVerificationToken> findByToken(String token);

    void deleteByEmail(String email);

    Optional<EmailVerificationToken> findByEmailAndIsUsedFalse(String email);
}
