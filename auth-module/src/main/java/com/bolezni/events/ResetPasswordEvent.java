package com.bolezni.events;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResetPasswordEvent extends BaseEvent<String> {
    private final String token;

    public ResetPasswordEvent(Object source, String data, String token) {
        super(source, data);
        this.token = token;
    }
}
