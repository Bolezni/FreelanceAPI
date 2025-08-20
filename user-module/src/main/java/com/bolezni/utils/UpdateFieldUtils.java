package com.bolezni.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class UpdateFieldUtils {

    private UpdateFieldUtils() {
    }

    /**
     * Обновляет строковое поле, если новое значение отличается от текущего
     * Выполняет trim и проверку на пустоту
     *
     * @param currentValue текущее значение поля
     * @param newValue     новое значение
     * @param setter       функция для установки значения
     * @return true если поле было обновлено
     */
    public static boolean updateStringField(String currentValue, String newValue, Consumer<String> setter) {
        String trimmedNew = trimToNull(newValue);
        String trimmedCurrent = trimToNull(currentValue);

        if (!Objects.equals(trimmedCurrent, trimmedNew)) {
            setter.accept(trimmedNew);
            return true;
        }
        return false;
    }

    /**
     * Обновляет поле любого типа, если новое значение отличается от текущего
     *
     * @param currentValue текущее значение поля
     * @param newValue     новое значение
     * @param setter       функция для установки значения
     * @return true если поле было обновлено
     */
    public static <T> boolean updateField(T currentValue, T newValue, Consumer<T> setter) {
        if (newValue != null && !Objects.equals(currentValue, newValue)) {
            setter.accept(newValue);
            return true;
        }
        return false;
    }

    /**
     * Обновляет поле используя getter и setter
     *
     * @param newValue новое значение
     * @param getter   функция получения текущего значения
     * @param setter   функция установки значения
     * @return true если поле было обновлено
     */
    public static <T> boolean updateField(T newValue, Supplier<T> getter, Consumer<T> setter) {
        return updateField(getter.get(), newValue, setter);
    }

    /**
     * Обновляет строковое поле используя getter и setter
     */
    public static boolean updateStringField(String newValue, Supplier<String> getter, Consumer<String> setter) {
        return updateStringField(getter.get(), newValue, setter);
    }


    /**
     * Обновляет числовое поле с дополнительными проверками
     */
    public static boolean updateNumericField(BigDecimal currentValue, BigDecimal newValue,
                                             Consumer<BigDecimal> setter, boolean allowNegative) {
        if (newValue != null) {
            if (!allowNegative && newValue.compareTo(BigDecimal.ZERO) < 0) {
                return false;
            }
            if (!Objects.equals(currentValue, newValue)) {
                setter.accept(newValue);
                return true;
            }
        }
        return false;
    }

    /**
     * Обновляет поле коллекции
     */
    public static <T> boolean updateCollectionField(Collection<T> currentValue, Collection<T> newValue,
                                                    Consumer<Collection<T>> setter) {
        if (newValue != null && !collectionsEqual(currentValue, newValue)) {
            // Создаем копию для безопасности
            setter.accept(new ArrayList<>(newValue));
            return true;
        }
        return false;
    }

    /**
     * Обновляет поле с кастомной валидацией
     */
    public static <T> boolean updateFieldWithValidation(T currentValue, T newValue,
                                                        Consumer<T> setter,
                                                        java.util.function.Predicate<T> validator) {
        if (newValue != null && validator.test(newValue) && !Objects.equals(currentValue, newValue)) {
            setter.accept(newValue);
            return true;
        }
        return false;
    }

    /**
     * Обновляет поле только если оно не null в источнике
     */
    public static <T> boolean updateFieldIfNotNull(T currentValue, T newValue, Consumer<T> setter) {
        if (newValue != null && !Objects.equals(currentValue, newValue)) {
            setter.accept(newValue);
            return true;
        }
        return false;
    }

    /**
     * Обновляет поле, позволяя установку null
     */
    public static <T> boolean updateFieldAllowNull(T currentValue, T newValue, Consumer<T> setter) {
        if (!Objects.equals(currentValue, newValue)) {
            setter.accept(newValue);
            return true;
        }
        return false;
    }

    /**
     * Выполняет несколько операций обновления и возвращает общий результат
     */
    @SafeVarargs
    public static boolean updateMultipleFields(java.util.function.Supplier<Boolean>... updateOperations) {
        boolean hasChanges = false;
        for (Supplier<Boolean> operation : updateOperations) {
            try {
                hasChanges |= operation.get();
            } catch (Exception e) {
                System.err.println("Error during field update: " + e.getMessage());
            }
        }
        return hasChanges;
    }

    private static String trimToNull(String str) {
        if (str == null) return null;
        String trimmed = str.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static <T> boolean collectionsEqual(Collection<T> c1, Collection<T> c2) {
        if (c1 == null && c2 == null) return true;
        if (c1 == null || c2 == null) return false;
        if (c1.size() != c2.size()) return false;
        return c1.containsAll(c2) && c2.containsAll(c1);
    }
}



