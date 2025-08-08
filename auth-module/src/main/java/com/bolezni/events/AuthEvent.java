package com.bolezni.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public abstract class AuthEvent<T> extends ApplicationEvent {
    private final T data;

    public AuthEvent(Object source, T data) {
        super(source);
        this.data = data;
    }
}

