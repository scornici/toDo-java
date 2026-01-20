package com.example.todo;

public record User(int id, String name, String displayName, String focusArea, Integer dailyGoal) {
    public String greetingName() {
        return displayName == null || displayName.isBlank() ? name : displayName;
    }
}
