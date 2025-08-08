package com.bolezni.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultEmailService implements EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            log.info("Email sent successfully to: {}", to);
        }catch (Exception e){
            log.error("Failed to send email to: {}. Error: {}", to, e.getMessage());
        }

    }

    @Override
    public void sendVerificationEmail(String to, String name, String verificationCode) {
        String subject = "Please verify your email address";
        String verificationUrl = baseUrl + "/auth/verify?token=" + verificationCode;

        String body = String.format(
                """
                        Hello %s,

                        Thank you for registering! Please click the link below to verify your email address:

                        %s

                        This link will expire in 24 hours.

                        If you didn't create this account, please ignore this email.

                        Best regards,
                        Your Application Team""",
                name, verificationUrl
        );

        sendEmail(to, subject, body);
    }
}
