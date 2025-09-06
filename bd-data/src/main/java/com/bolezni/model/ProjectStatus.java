package com.bolezni.model;

public enum ProjectStatus {
    PENDING("Ожидает"),
    IN_PROGRESS("В процессе"),
    COMPLETED("Выполнен"),
    CANCEL("Отменен");

    final String value;

    ProjectStatus(String value) {
        this.value = value;
    }
}
