package com.example.todo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ToDoRepository {
    private final Database database;

    public ToDoRepository(Database database) {
        this.database = database;
    }

    public User ensureUser(String name) {
        Optional<User> existing = findUserByName(name);
        if (existing.isPresent()) {
            return existing.get();
        }
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(
                 "INSERT INTO users(name) VALUES (?)",
                 Statement.RETURN_GENERATED_KEYS
             )) {
            statement.setString(1, name);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new User(keys.getInt(1), name, null, null, null);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create user", ex);
        }
        throw new IllegalStateException("Failed to create user; no ID returned");
    }

    public Optional<User> findUserByName(String name) {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT id, name, display_name, focus_area, daily_goal FROM users WHERE name = ?"
             )) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new User(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("display_name"),
                        resultSet.getString("focus_area"),
                        (Integer) resultSet.getObject("daily_goal")
                    ));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to fetch user", ex);
        }
        return Optional.empty();
    }

    public User updateProfile(int userId, String displayName, String focusArea, Integer dailyGoal) {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(
                 "UPDATE users SET display_name = ?, focus_area = ?, daily_goal = ? WHERE id = ?"
             )) {
            statement.setString(1, displayName == null || displayName.isBlank() ? null : displayName);
            statement.setString(2, focusArea == null || focusArea.isBlank() ? null : focusArea);
            if (dailyGoal == null) {
                statement.setNull(3, java.sql.Types.INTEGER);
            } else {
                statement.setInt(3, dailyGoal);
            }
            statement.setInt(4, userId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update profile", ex);
        }
        return findUserById(userId)
            .orElseThrow(() -> new IllegalStateException("Failed to reload user profile"));
    }

    private Optional<User> findUserById(int userId) {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT id, name, display_name, focus_area, daily_goal FROM users WHERE id = ?"
             )) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new User(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("display_name"),
                        resultSet.getString("focus_area"),
                        (Integer) resultSet.getObject("daily_goal")
                    ));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to fetch user", ex);
        }
        return Optional.empty();
    }

    public List<Task> fetchTasks(int userId) {
        List<Task> tasks = new ArrayList<>();
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT id, title, notes, due_date, completed, created_at FROM tasks WHERE user_id = ? ORDER BY completed, due_date, created_at"
             )) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tasks.add(new Task(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("notes"),
                        resultSet.getString("due_date"),
                        resultSet.getInt("completed") == 1,
                        resultSet.getString("created_at")
                    ));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to fetch tasks", ex);
        }
        return tasks;
    }

    public void addTask(int userId, String title, String notes, String dueDate) {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(
                 "INSERT INTO tasks(user_id, title, notes, due_date) VALUES (?, ?, ?, ?)"
             )) {
            statement.setInt(1, userId);
            statement.setString(2, title);
            statement.setString(3, notes == null || notes.isBlank() ? null : notes);
            statement.setString(4, dueDate == null || dueDate.isBlank() ? null : dueDate);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to add task", ex);
        }
    }

    public boolean markComplete(int userId, int taskId) {
        return updateTaskCompletion(userId, taskId, true);
    }

    public boolean markIncomplete(int userId, int taskId) {
        return updateTaskCompletion(userId, taskId, false);
    }

    private boolean updateTaskCompletion(int userId, int taskId, boolean completed) {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(
                 "UPDATE tasks SET completed = ? WHERE id = ? AND user_id = ?"
             )) {
            statement.setInt(1, completed ? 1 : 0);
            statement.setInt(2, taskId);
            statement.setInt(3, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update task", ex);
        }
    }

    public boolean deleteTask(int userId, int taskId) {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(
                 "DELETE FROM tasks WHERE id = ? AND user_id = ?"
             )) {
            statement.setInt(1, taskId);
            statement.setInt(2, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete task", ex);
        }
    }

    public TaskStats fetchStats(int userId) {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT COUNT(*) AS total, SUM(CASE WHEN completed = 1 THEN 1 ELSE 0 END) AS done FROM tasks WHERE user_id = ?"
             )) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int total = resultSet.getInt("total");
                    int done = resultSet.getInt("done");
                    return new TaskStats(total, done);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to fetch stats", ex);
        }
        return new TaskStats(0, 0);
    }
}
