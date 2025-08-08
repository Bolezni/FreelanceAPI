package com.bolezni.listener;

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
            emailService.sendVerificationEmail(
                    event.getData(),
                    event.getFirstname(),
                    event.getVerificationToken()

            );
        } catch (Exception e) {
            log.error("Failed to process email notification for user: {}. Error: {}",
                    event.getData(), e.getMessage());
        }
    }
}
