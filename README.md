# Personalized To-Do (SQLite)

A simple personalized to-do list written in Java with an embedded SQLite database. Each user gets their own tasks and progress stats.

## Features
- Create or sign in by name
- Add tasks with optional notes and due dates
- Personalize your profile with display name, focus area, and daily goal
- Mark tasks complete/incomplete
- Delete tasks
- Persistent storage with SQLite (`todo.db`)

## Requirements
- Java 17+
- Maven

## Run
```bash
mvn -q package
java -jar target/todo-sqlite-1.0.0.jar
```

The database file (`todo.db`) is created in the working directory.
