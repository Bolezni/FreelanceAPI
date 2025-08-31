package com.bolezni.service.impl;

import com.bolezni.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DefaultEmailService implements EmailService {
    private final JavaMailSender mailSender;

    public DefaultEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        log.info("EmailService initialized!");
    }


    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            log.info("Sending email from " + message.getFrom());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}. Error: {}", to, e.getMessage());
        }

    }

    @Override
    public void sendVerificationEmail(String to, String name, String tokenValue, String verificationCode) {
        String subject = "Please verify your email address";
        String token = "http://localhost:8090" + "/api/v1/auth/verify?token=" + tokenValue;

        String body = String.format(
                """
                        Hello %s,

                        Thank you for registering! Please click the link below to verify your email address:

                        %s
                        
                        or write this code: %s

                        This link will expire in 24 hours.

                        If you didn't create this account, please ignore this email.

                        Best regards,
                        Your Application Team""",
                name, token, verificationCode
        );

        sendEmail(to, subject, body);
    }
}
