package com.example.todo;

public record Task(int id, String title, String notes, String dueDate, boolean completed, String createdAt) {
    public String completionLabel() {
        return completed ? "✓" : " ";
    }
}
