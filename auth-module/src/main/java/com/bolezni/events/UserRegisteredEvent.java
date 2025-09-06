package com.bolezni.events;

import lombok.Getter;

@Getter
public class UserRegisteredEvent extends BaseEvent<String> {
    private final String firstname;
    private final String token;
    private final String verificationToken;

    public UserRegisteredEvent(Object source, String data, String firstname, String token, String verificationToken) {
        super(source, data);
        this.firstname = firstname;
        this.token = token;
        this.verificationToken = verificationToken;
    }
}
