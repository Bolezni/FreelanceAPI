package com.bolezni.events;

import lombok.Getter;

@Getter
public class UserRegisteredEvent extends AuthEvent<String> {
    private final String firstname;
    private final String verificationToken;

    public UserRegisteredEvent(Object source, String data, String firstname, String verificationToken) {
        super(source, data);
        this.firstname = firstname;
        this.verificationToken = verificationToken;
    }
}
