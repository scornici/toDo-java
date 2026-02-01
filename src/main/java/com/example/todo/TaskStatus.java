package com.example.todo;

public enum TaskStatus {
    COMPLETE("Completed", true),
    IN_PROGRESS("In progress", false);

    private final String label;
    private final boolean completed;

    TaskStatus(String label, boolean completed) {
        this.label = label;
        this.completed = completed;
    }

    public static TaskStatus fromCompleted(boolean completed) {
        return completed ? COMPLETE : IN_PROGRESS;
    }

    public String label() {
        return label;
    }

    public boolean isCompleted() {
        return completed;
    }
}
