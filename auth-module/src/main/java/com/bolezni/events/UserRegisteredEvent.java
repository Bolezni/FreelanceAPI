package com.bolezni.events;

import lombok.Getter;

@Getter
public class UserRegisteredEvent extends AuthEvent<String> {
    public UserRegisteredEvent(Object source, String data) {
        super(source, data);
    }
}
