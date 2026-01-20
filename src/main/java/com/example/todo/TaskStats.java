package com.example.todo;

public record TaskStats(int total, int completed) {
    public int remaining() {
        return Math.max(0, total - completed);
    }
}
