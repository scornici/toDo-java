package com.example.todo;

public class Task {
    private final int id;
    private final String title;
    private final String notes;
    private final String dueDate;
    private final boolean completed;
    private final String createdAt;
    private TaskStatus status;

    public Task(int id, String title, String notes, String dueDate, boolean completed, String createdAt, TaskStatus status) {
        this.id = id;
        this.title = title;
        this.notes = notes;
        this.dueDate = dueDate;
        this.completed = completed;
        this.createdAt = createdAt;
        this.status = status == null ? TaskStatus.TODO : status;
    }

    public Task(int id, String title, String notes, String dueDate, boolean completed, String createdAt) {
        this(id, title, notes, dueDate, completed, createdAt, TaskStatus.TODO);
    }

    public int id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String notes() {
        return notes;
    }

    public String dueDate() {
        return dueDate;
    }

    public boolean completed() {
        return completed;
    }

    public String createdAt() {
        return createdAt;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status == null ? TaskStatus.TODO : status;
    }

    public String completionLabel() {
        return completed ? "✓" : " ";
    }
}
