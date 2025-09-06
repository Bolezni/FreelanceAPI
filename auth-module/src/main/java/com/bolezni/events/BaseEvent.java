package com.bolezni.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public abstract class BaseEvent<T> extends ApplicationEvent {
    private final T data;

    public BaseEvent(Object source, T data) {
        super(source);
        this.data = data;
    }
}

