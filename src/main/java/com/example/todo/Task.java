package com.example.todo;

import java.io.Serial;
import java.io.Serializable;

public record Task(
        int id,
        String title,
        String notes,
        String dueDate,
        boolean completed,
        String createdAt,
        TaskStatus status
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public String completionLabel() {
        return completed ? "✓" : " ";
    }
}
