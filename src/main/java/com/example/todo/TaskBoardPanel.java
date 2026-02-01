package com.example.todo;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import java.util.List;

public class TaskBoardPanel extends JPanel {
    private final ToDoRepository repository;
    private final Runnable onTaskChange;
    private final TaskColumnPanel todoColumn;
    private final TaskColumnPanel doingColumn;
    private final TaskColumnPanel doneColumn;
    private TaskCard selectedCard;
    private int activeUserId;

    public TaskBoardPanel(ToDoRepository repository, Runnable onTaskChange) {
        this.repository = repository;
        this.onTaskChange = onTaskChange;
        setLayout(new GridLayout(1, 3, 16, 0));
        setBackground(ToDoApp.BACKGROUND);
        setBorder(new EmptyBorder(8, 8, 8, 8));

        todoColumn = new TaskColumnPanel("TODO", TaskStatus.TODO, this::handleDrop);
        doingColumn = new TaskColumnPanel("DOING", TaskStatus.DOING, this::handleDrop);
        doneColumn = new TaskColumnPanel("DONE", TaskStatus.DONE, this::handleDrop);

        add(todoColumn);
        add(doingColumn);
        add(doneColumn);
    }

    public void refresh(int userId) {
        this.activeUserId = userId;
        clearSelection();
        populate(todoColumn, repository.fetchTasksByStatus(userId, TaskStatus.TODO));
        populate(doingColumn, repository.fetchTasksByStatus(userId, TaskStatus.DOING));
        populate(doneColumn, repository.fetchTasksByStatus(userId, TaskStatus.DONE));
    }

    public Task getSelectedTask() {
        return selectedCard == null ? null : selectedCard.task();
    }

    private void populate(TaskColumnPanel column, List<Task> tasks) {
        column.setTasks(tasks, this::selectCard);
    }

    private void handleDrop(int taskId, TaskStatus status) {
        boolean updated = repository.updateTaskStatus(activeUserId, taskId, status);
        if (updated) {
            onTaskChange.run();
        }
    }

    private void selectCard(TaskCard card) {
        if (selectedCard != null) {
            selectedCard.setSelected(false);
        }
        selectedCard = card;
        if (selectedCard != null) {
            selectedCard.setSelected(true);
        }
    }

    private void clearSelection() {
        if (selectedCard != null) {
            selectedCard.setSelected(false);
            selectedCard = null;
        }
    }
}
