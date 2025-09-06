package com.bolezni.listener;

import com.bolezni.events.ResetPasswordEvent;
import com.bolezni.events.UserRegisteredEvent;
import com.bolezni.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationListener {

    private final EmailService emailService;

    @EventListener
    @Async("emailExecutor")
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            log.info("Processing email notification for user: {}", event);
            log.info("Email: "+ event.getData());
            log.info("Code: " + event.getVerificationToken());
            emailService.sendVerificationEmail(
                    event.getData(),
                    event.getFirstname(),
                    event.getToken(),
                    event.getVerificationToken()

            );
        } catch (Exception e) {
            log.error("Failed to process email notification for user: {}. Error: {}",
                    event.getData(), e.getMessage());
        }
    }
    
    @EventListener
    @Async("emailExecutor")
    public void handleUserResetPassword(ResetPasswordEvent event) {
        try{
            log.info("Processing email notification for user: {}", event);

            emailService.sendEmail(
                    event.getData(),
                    "",
                    event.getToken()
            );
        }catch (Exception e) {
            log.error("Failed to process email notification for user: {}. Error: {}",
                    event.getData(), e.getMessage());
        }
    }
}
