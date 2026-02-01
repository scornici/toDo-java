package com.example.todo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class Database {
    private final String jdbcUrl;

    public Database(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }

    public void initialize() {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    display_name TEXT,
                    focus_area TEXT,
                    daily_goal INTEGER
                )
                """);
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    notes TEXT,
                    due_date TEXT,
                    completed INTEGER NOT NULL DEFAULT 0,
                    status TEXT NOT NULL DEFAULT 'TODO',
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(user_id) REFERENCES users(id)
                )
                """);
            ensureUserColumns(connection);
            ensureTaskColumns(connection);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize database", ex);
        }
    }

    private void ensureUserColumns(Connection connection) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement("PRAGMA table_info(users)");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("name"));
            }
        }
        addColumnIfMissing(connection, "users", columns, "display_name", "TEXT");
        addColumnIfMissing(connection, "users", columns, "focus_area", "TEXT");
        addColumnIfMissing(connection, "users", columns, "daily_goal", "INTEGER");
    }

    private void ensureTaskColumns(Connection connection) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement("PRAGMA table_info(tasks)");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("name"));
            }
        }
        addColumnIfMissing(connection, "tasks", columns, "status", "TEXT NOT NULL DEFAULT 'TODO'");
    }

    private void addColumnIfMissing(Connection connection, String tableName, Set<String> columns, String column, String type)
        throws SQLException {
        if (!columns.contains(column)) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + column + " " + type);
            }
        }
    }

    private void ensureTaskColumns(Connection connection) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement("PRAGMA table_info(tasks)");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("name"));
            }
        }
        if (!columns.contains("status")) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE tasks ADD COLUMN status TEXT NOT NULL DEFAULT 'TODO'");
            }
        }
    }
}
