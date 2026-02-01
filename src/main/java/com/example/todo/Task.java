package com.example.todo;

public record Task(int id, String title, String notes, String dueDate, boolean completed, String createdAt) {
    public TaskStatus status() {
        return TaskStatus.fromCompleted(completed);
    }

    public String completionLabel() {
        return completed ? "✓" : " ";
    }
}
