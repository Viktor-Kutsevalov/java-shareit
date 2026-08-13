package ru.practicum.shareit.booking;

import ru.practicum.shareit.exception.ValidationException;

public enum BookingState {
    ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED;

    public static BookingState from(String state) {
        if (state == null) return ALL;
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Неизвестный статус: " + state);
        }
    }
}
