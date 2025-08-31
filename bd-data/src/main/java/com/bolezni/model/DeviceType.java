package com.bolezni.model;

import lombok.Getter;

@Getter
public enum DeviceType {
    WEB("web"),
    ANDROID("android"),
    IOS("ios"),
    DESKTOP("desktop");

    private final String value;

    DeviceType(String value) {
        this.value = value;
    }
}
