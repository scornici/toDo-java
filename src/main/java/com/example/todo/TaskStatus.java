package com.example.todo;

import java.io.Serial;
import java.io.Serializable;

public enum TaskStatus implements Serializable {
    TODO,
    DOING,
    DONE;

    @Serial
    private static final long serialVersionUID = 1L;

    public static TaskStatus fromDatabase(String value) {
        if (value == null || value.isBlank()) {
            return TODO;
        }
        try {
            return TaskStatus.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return TODO;
        }

    }
}
