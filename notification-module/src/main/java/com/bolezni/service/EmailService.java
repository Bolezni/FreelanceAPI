package com.bolezni.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
    void sendVerificationEmail(String to, String name, String tokenValue,String verificationCode);
}
