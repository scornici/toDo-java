package com.example.todo;

public enum TaskStatus {
    TODO,
    DOING,
    DONE;

    public static TaskStatus fromDatabase(String value) {
        if (value == null) {
            return TODO;
        }
        try {
            return TaskStatus.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return TODO;
        }
    }
}
