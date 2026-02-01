package com.example.todo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ToDoApp {
    private static final String JDBC_URL = "jdbc:sqlite:todo.db";
    static final Color BACKGROUND = new Color(245, 246, 250);
    static final Color PANEL = new Color(255, 255, 255);
    static final Color ACCENT = new Color(92, 103, 242);
    static final Color TEXT_PRIMARY = new Color(38, 40, 55);
    static final Color TEXT_SECONDARY = new Color(108, 112, 132);

    public static void main(String[] args) {
        Database database = new Database(JDBC_URL);
        database.initialize();

        ToDoRepository repository = new ToDoRepository(database);
        SwingUtilities.invokeLater(() -> {
            configureLookAndFeel();
            new ToDoWindow(repository).show();
        });
    }

    private static void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Use default look and feel if system look fails.
        }
        Font uiFont = new Font("SansSerif", Font.PLAIN, 14);
        UIManager.put("Label.font", uiFont);
        UIManager.put("Button.font", uiFont);
        UIManager.put("TextField.font", uiFont);
        UIManager.put("TextArea.font", uiFont);
        UIManager.put("List.font", uiFont);
    }

    private static final class ToDoWindow {
        private final ToDoRepository repository;
        private final JFrame frame;
        private final JLabel greetingLabel;
        private final JLabel statsLabel;
        private final JLabel focusLabel;
        private final JLabel goalLabel;
        private final TaskBoardPanel taskBoard;
        private User activeUser;

        private ToDoWindow(ToDoRepository repository) {
            this.repository = repository;
            frame = new JFrame("Daily Tasks");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(920, 620));
            frame.getContentPane().setBackground(BACKGROUND);
            frame.setLayout(new BorderLayout());

            JPanel header = createHeader();
            JPanel content = createContent();

            frame.add(header, BorderLayout.NORTH);
            frame.add(content, BorderLayout.CENTER);

            greetingLabel = createHeaderLabel();
            statsLabel = createSecondaryLabel();
            focusLabel = createSecondaryLabel();
            goalLabel = createSecondaryLabel();

            header.add(greetingLabel, BorderLayout.NORTH);
            header.add(statsLabel, BorderLayout.CENTER);

            JPanel subHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
            subHeader.setBackground(PANEL);
            subHeader.add(focusLabel);
            subHeader.add(goalLabel);
            header.add(subHeader, BorderLayout.SOUTH);

            JPanel listCard = new JPanel(new BorderLayout());
            listCard.setBackground(PANEL);
            listCard.setBorder(new EmptyBorder(24, 24, 24, 24));
            taskBoard = new TaskBoardPanel(repository, this::refresh);
            listCard.add(taskBoard, BorderLayout.CENTER);

            JPanel actions = createActions();
            content.add(listCard, BorderLayout.CENTER);
            content.add(actions, BorderLayout.EAST);
        }

        private JPanel createHeader() {
            JPanel header = new JPanel(new BorderLayout());
            header.setBorder(new EmptyBorder(24, 32, 20, 32));
            header.setBackground(PANEL);
            return header;
        }

        private JPanel createContent() {
            JPanel content = new JPanel(new BorderLayout(24, 24));
            content.setBorder(new EmptyBorder(24, 32, 32, 32));
            content.setBackground(BACKGROUND);
            return content;
        }

        private JLabel createHeaderLabel() {
            JLabel label = new JLabel();
            label.setFont(new Font("SansSerif", Font.BOLD, 24));
            label.setForeground(TEXT_PRIMARY);
            return label;
        }

        private JLabel createSecondaryLabel() {
            JLabel label = new JLabel();
            label.setFont(new Font("SansSerif", Font.PLAIN, 14));
            label.setForeground(TEXT_SECONDARY);
            return label;
        }

        private JPanel createActions() {
            JPanel actions = new JPanel();
            actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
            actions.setBackground(BACKGROUND);
            actions.setBorder(new EmptyBorder(0, 0, 0, 0));

            JButton addTask = primaryButton("Add Task");
            addTask.addActionListener(event -> addTask());
            JButton completeTask = ghostButton("Mark Complete");
            completeTask.addActionListener(event -> updateCompletion(true));
            JButton reopenTask = ghostButton("Mark Incomplete");
            reopenTask.addActionListener(event -> updateCompletion(false));
            JButton deleteTask = ghostButton("Delete Task");
            deleteTask.setForeground(new Color(200, 72, 72));
            deleteTask.addActionListener(event -> deleteTask());

            JButton updateProfile = ghostButton("Update Profile");
            updateProfile.addActionListener(event -> updateProfile());
            JButton switchUser = ghostButton("Switch User");
            switchUser.addActionListener(event -> switchUser());

            actions.add(addTask);
            actions.add(Box.createVerticalStrut(12));
            actions.add(completeTask);
            actions.add(Box.createVerticalStrut(8));
            actions.add(reopenTask);
            actions.add(Box.createVerticalStrut(8));
            actions.add(deleteTask);
            actions.add(Box.createVerticalStrut(24));
            actions.add(updateProfile);
            actions.add(Box.createVerticalStrut(8));
            actions.add(switchUser);
            actions.add(Box.createVerticalGlue());

            return actions;
        }

        private JButton primaryButton(String label) {
            JButton button = new JButton(label);
            styleButton(button, ACCENT, Color.WHITE);
            return button;
        }

        private JButton ghostButton(String label) {
            JButton button = new JButton(label);
            styleButton(button, PANEL, TEXT_PRIMARY);
            button.setBorder(BorderFactory.createLineBorder(new Color(220, 223, 232)));
            return button;
        }

        private void styleButton(JButton button, Color background, Color foreground) {
            button.setBackground(background);
            button.setForeground(foreground);
            button.setFocusPainted(false);
            button.setBorder(new EmptyBorder(10, 18, 10, 18));
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        private void show() {
            User user = promptForUser();
            if (user == null) {
                return;
            }
            activeUser = user;
            refresh();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }

        private User promptForUser() {
            while (true) {
                String name = JOptionPane.showInputDialog(frame, "Enter your name", "Welcome", JOptionPane.PLAIN_MESSAGE);
                if (name == null) {
                    frame.dispose();
                    return null;
                }
                name = name.trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Name cannot be empty.", "Try Again", JOptionPane.WARNING_MESSAGE);
                    continue;
                }
                User user = repository.ensureUser(name);
                return maybePersonalizeProfile(user);
            }
        }

        private User maybePersonalizeProfile(User user) {
            if (user.displayName() != null || user.focusArea() != null || user.dailyGoal() != null) {
                return user;
            }
            int response = JOptionPane.showConfirmDialog(
                frame,
                "Would you like to personalize your profile now?",
                "Personalize",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (response == JOptionPane.YES_OPTION) {
                return showProfileDialog(user);
            }
            return user;
        }

        private void refresh() {
            TaskStats stats = repository.fetchStats(activeUser.id());
            greetingLabel.setText(String.format("Welcome back, %s", activeUser.greetingName()));
            statsLabel.setText(String.format("%d tasks · %d completed · %d remaining",
                stats.total(),
                stats.completed(),
                stats.remaining()));

            if (activeUser.focusArea() != null && !activeUser.focusArea().isBlank()) {
                focusLabel.setText("Focus: " + activeUser.focusArea());
            } else {
                focusLabel.setText("Set a focus area to stay centered");
            }

            if (activeUser.dailyGoal() != null && activeUser.dailyGoal() > 0) {
                int remaining = Math.max(0, activeUser.dailyGoal() - stats.completed());
                goalLabel.setText(String.format("Daily goal: %d left", remaining));
            } else {
                goalLabel.setText("Set a daily goal to keep momentum");
            }

            taskBoard.refresh(activeUser.id());
        }

        private void addTask() {
            TaskForm form = TaskForm.empty();
            int result = form.showDialog(frame, "Add a task");
            if (result == JOptionPane.OK_OPTION) {
                if (form.title().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Task title cannot be empty.", "Missing info", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                repository.addTask(activeUser.id(), form.title(), form.notes(), form.dueDate());
                refresh();
            }
        }

        private void updateCompletion(boolean completed) {
            Task selected = taskBoard.getSelectedTask();
            if (selected == null) {
                JOptionPane.showMessageDialog(frame, "Select a task first.", "No task selected", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            TaskStatus status = completed ? TaskStatus.DONE : TaskStatus.TODO;
            boolean updated = repository.updateTaskStatus(activeUser.id(), selected.id(), status);
            if (updated) {
                refresh();
            } else {
                JOptionPane.showMessageDialog(frame, "Task could not be updated.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void deleteTask() {
            Task selected = taskBoard.getSelectedTask();
            if (selected == null) {
                JOptionPane.showMessageDialog(frame, "Select a task first.", "No task selected", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int response = JOptionPane.showConfirmDialog(
                frame,
                "Delete selected task?",
                "Confirm",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (response == JOptionPane.YES_OPTION) {
                repository.deleteTask(activeUser.id(), selected.id());
                refresh();
            }
        }

        private void updateProfile() {
            activeUser = showProfileDialog(activeUser);
            refresh();
        }

        private User showProfileDialog(User user) {
            ProfileForm form = ProfileForm.from(user);
            int result = form.showDialog(frame, "Update profile");
            if (result == JOptionPane.OK_OPTION) {
                return repository.updateProfile(user.id(), form.displayName(), form.focusArea(), form.dailyGoal());
            }
            return user;
        }

        private void switchUser() {
            User user = promptForUser();
            if (user != null) {
                activeUser = user;
                refresh();
            }
        }
    }


    private record TaskForm(JTextField titleField, JTextArea notesField, JTextField dueDateField) {
        private static TaskForm empty() {
            JTextField titleField = new JTextField();
            JTextArea notesField = new JTextArea(4, 20);
            notesField.setLineWrap(true);
            notesField.setWrapStyleWord(true);
            JTextField dueDateField = new JTextField();
            return new TaskForm(titleField, notesField, dueDateField);
        }

        private String title() {
            return titleField.getText().trim();
        }

        private String notes() {
            String notes = notesField.getText().trim();
            return notes.isEmpty() ? null : notes;
        }

        private String dueDate() {
            String dueDate = dueDateField.getText().trim();
            return dueDate.isEmpty() ? null : dueDate;
        }

        private int showDialog(Component parent, String title) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(new JLabel("Task title"));
            panel.add(titleField);
            panel.add(Box.createVerticalStrut(8));
            panel.add(new JLabel("Notes"));
            panel.add(new JScrollPane(notesField));
            panel.add(Box.createVerticalStrut(8));
            panel.add(new JLabel("Due date"));
            panel.add(dueDateField);
            panel.setPreferredSize(new Dimension(420, 260));

            JDialog.setDefaultLookAndFeelDecorated(true);
            return JOptionPane.showConfirmDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        }
    }

    private record ProfileForm(JTextField displayNameField, JTextField focusField, JTextField goalField) {
        private static ProfileForm from(User user) {
            JTextField displayName = new JTextField(user.displayName() == null ? "" : user.displayName());
            JTextField focus = new JTextField(user.focusArea() == null ? "" : user.focusArea());
            JTextField goal = new JTextField(user.dailyGoal() == null ? "" : String.valueOf(user.dailyGoal()));
            return new ProfileForm(displayName, focus, goal);
        }

        private String displayName() {
            String value = displayNameField.getText().trim();
            return value.isEmpty() ? null : value;
        }

        private String focusArea() {
            String value = focusField.getText().trim();
            return value.isEmpty() ? null : value;
        }

        private Integer dailyGoal() {
            String value = goalField.getText().trim();
            if (value.isEmpty()) {
                return null;
            }
            try {
                int parsed = Integer.parseInt(value);
                return parsed > 0 ? parsed : null;
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        private int showDialog(Component parent, String title) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(new JLabel("Display name"));
            panel.add(displayNameField);
            panel.add(Box.createVerticalStrut(8));
            panel.add(new JLabel("Focus area"));
            panel.add(focusField);
            panel.add(Box.createVerticalStrut(8));
            panel.add(new JLabel("Daily goal"));
            panel.add(goalField);
            panel.setPreferredSize(new Dimension(380, 220));

            JDialog.setDefaultLookAndFeelDecorated(true);
            return JOptionPane.showConfirmDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        }
    }
}
