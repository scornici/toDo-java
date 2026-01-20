package com.example.todo;

import java.util.List;
import java.util.Scanner;

public class ToDoApp {
    private static final String JDBC_URL = "jdbc:sqlite:todo.db";

    public static void main(String[] args) {
        Database database = new Database(JDBC_URL);
        database.initialize();

        ToDoRepository repository = new ToDoRepository(database);
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Welcome to your personalized to-do list!");
            User activeUser = promptForUser(scanner, repository);

            boolean running = true;
            while (running) {
                TaskStats stats = repository.fetchStats(activeUser.id());
                System.out.printf("\nHi %s! You have %d tasks (%d completed, %d remaining).%n",
                    activeUser.greetingName(),
                    stats.total(),
                    stats.completed(),
                    stats.remaining());
                printProfileSummary(activeUser, stats);
                System.out.println("1) View tasks");
                System.out.println("2) Add a task");
                System.out.println("3) Mark task complete");
                System.out.println("4) Mark task incomplete");
                System.out.println("5) Delete a task");
                System.out.println("6) Update profile");
                System.out.println("7) Switch user");
                System.out.println("8) Exit");
                System.out.print("Choose an option: ");

                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1" -> displayTasks(repository, activeUser);
                    case "2" -> addTask(scanner, repository, activeUser);
                    case "3" -> updateCompletion(scanner, repository, activeUser, true);
                    case "4" -> updateCompletion(scanner, repository, activeUser, false);
                    case "5" -> deleteTask(scanner, repository, activeUser);
                    case "6" -> activeUser = updateProfile(scanner, repository, activeUser);
                    case "7" -> activeUser = promptForUser(scanner, repository);
                    case "8" -> running = false;
                    default -> System.out.println("Please enter a number from the menu.");
                }
            }
        }
        System.out.println("Goodbye! Your tasks are saved in todo.db.");
    }

    private static User promptForUser(Scanner scanner, ToDoRepository repository) {
        while (true) {
            System.out.print("Enter your name: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Name cannot be empty.");
                continue;
            }
            User user = repository.ensureUser(name);
            System.out.printf("Welcome, %s!%n", user.greetingName());
            return maybePersonalizeProfile(scanner, repository, user);
        }
    }

    private static void displayTasks(ToDoRepository repository, User user) {
        List<Task> tasks = repository.fetchTasks(user.id());
        if (tasks.isEmpty()) {
            System.out.println("No tasks yet. Add one from the menu!");
            return;
        }
        System.out.println("\nYour tasks:");
        for (Task task : tasks) {
            String due = task.dueDate() == null ? "No due date" : task.dueDate();
            String notes = task.notes() == null ? "" : " - " + task.notes();
            System.out.printf("[%s] #%d %s (Due: %s)%s%n",
                task.completionLabel(),
                task.id(),
                task.title(),
                due,
                notes);
        }
    }

    private static User maybePersonalizeProfile(Scanner scanner, ToDoRepository repository, User user) {
        if (user.displayName() != null || user.focusArea() != null || user.dailyGoal() != null) {
            return user;
        }
        System.out.print("Would you like to personalize your profile now? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("y") || response.equals("yes")) {
            return updateProfile(scanner, repository, user);
        }
        return user;
    }

    private static User updateProfile(Scanner scanner, ToDoRepository repository, User user) {
        System.out.print("Preferred display name (blank keeps current, type 'clear' to remove): ");
        String displayName = normalizeProfileInput(scanner.nextLine().trim(), user.displayName());
        System.out.print("Focus area (blank keeps current, type 'clear' to remove): ");
        String focusArea = normalizeProfileInput(scanner.nextLine().trim(), user.focusArea());
        Integer dailyGoal = user.dailyGoal();
        System.out.print("Daily task goal (blank keeps current, type 'clear' to remove): ");
        String goalInput = scanner.nextLine().trim();
        if (goalInput.equalsIgnoreCase("clear")) {
            dailyGoal = null;
        } else if (!goalInput.isEmpty()) {
            try {
                int parsedGoal = Integer.parseInt(goalInput);
                if (parsedGoal > 0) {
                    dailyGoal = parsedGoal;
                } else {
                    System.out.println("Daily goal must be positive. Keeping current value.");
                }
            } catch (NumberFormatException ex) {
                System.out.println("That wasn't a number. Keeping current value.");
            }
        }
        User updated = repository.updateProfile(user.id(), displayName, focusArea, dailyGoal);
        System.out.println("Profile updated.");
        return updated;
    }

    private static String normalizeProfileInput(String input, String currentValue) {
        if (input.equalsIgnoreCase("clear")) {
            return null;
        }
        if (input.isEmpty()) {
            return currentValue;
        }
        return input;
    }

    private static void printProfileSummary(User user, TaskStats stats) {
        boolean hasDetails = false;
        if (user.displayName() != null
            && !user.displayName().isBlank()
            && !user.displayName().equalsIgnoreCase(user.name())) {
            System.out.printf("Display name: %s%n", user.displayName());
            hasDetails = true;
        }
        if (user.focusArea() != null && !user.focusArea().isBlank()) {
            System.out.printf("Focus: %s%n", user.focusArea());
            hasDetails = true;
        }
        if (user.dailyGoal() != null && user.dailyGoal() > 0) {
            int remaining = Math.max(0, user.dailyGoal() - stats.completed());
            System.out.printf("Daily goal: %d completed, %d to go%n", stats.completed(), remaining);
            hasDetails = true;
        }
        if (!hasDetails) {
            System.out.println("Tip: update your profile to personalize your daily plan.");
        }
    }

    private static void addTask(Scanner scanner, ToDoRepository repository, User user) {
        System.out.print("Task title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("Task title cannot be empty.");
            return;
        }
        System.out.print("Notes (optional): ");
        String notes = scanner.nextLine().trim();
        System.out.print("Due date (optional, e.g. 2024-12-31): ");
        String dueDate = scanner.nextLine().trim();
        repository.addTask(user.id(), title, notes, dueDate);
        System.out.println("Task added!");
    }

    private static void updateCompletion(Scanner scanner, ToDoRepository repository, User user, boolean completed) {
        System.out.print("Enter task ID: ");
        String input = scanner.nextLine().trim();
        try {
            int taskId = Integer.parseInt(input);
            boolean updated = completed
                ? repository.markComplete(user.id(), taskId)
                : repository.markIncomplete(user.id(), taskId);
            if (updated) {
                System.out.println("Task updated.");
            } else {
                System.out.println("Task not found.");
            }
        } catch (NumberFormatException ex) {
            System.out.println("Please enter a valid numeric task ID.");
        }
    }

    private static void deleteTask(Scanner scanner, ToDoRepository repository, User user) {
        System.out.print("Enter task ID to delete: ");
        String input = scanner.nextLine().trim();
        try {
            int taskId = Integer.parseInt(input);
            if (repository.deleteTask(user.id(), taskId)) {
                System.out.println("Task deleted.");
            } else {
                System.out.println("Task not found.");
            }
        } catch (NumberFormatException ex) {
            System.out.println("Please enter a valid numeric task ID.");
        }
    }
}
