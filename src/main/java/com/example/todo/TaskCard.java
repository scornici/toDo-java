package com.example.todo;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.JComponent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class TaskCard extends JPanel {
    private static final Color CARD_SELECTED = new Color(234, 236, 244);

    private final Task task;
    private final JLabel titleLabel;
    private final JLabel metaLabel;
    private final JLabel statusLabel;
    private boolean selected;

    public TaskCard(Task task, Consumer<TaskCard> onSelect) {
        this.task = task;
        setLayout(new BorderLayout(8, 6));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setBackground(ToDoApp.PANEL);

        titleLabel = new JLabel(task.title());
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(ToDoApp.TEXT_PRIMARY);

        metaLabel = new JLabel(buildMeta(task));
        metaLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        metaLabel.setForeground(ToDoApp.TEXT_SECONDARY);

        statusLabel = new JLabel(task.status().name(), SwingConstants.RIGHT);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(ToDoApp.TEXT_SECONDARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(titleLabel, BorderLayout.CENTER);
        header.add(statusLabel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(metaLabel, BorderLayout.CENTER);

        setTransferHandler(new TaskCardTransferHandler(task));
        MouseAdapter dragHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                onSelect.accept(TaskCard.this);
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                TransferHandler handler = getTransferHandler();
                handler.exportAsDrag(TaskCard.this, event, TransferHandler.MOVE);
            }
        };
        addMouseListener(dragHandler);
        addMouseMotionListener(dragHandler);
    }

    public Task task() {
        return task;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        setBackground(selected ? CARD_SELECTED : ToDoApp.PANEL);
        repaint();
    }

    private String buildMeta(Task task) {
        String dueDate = task.dueDate() == null || task.dueDate().isBlank() ? "No due date" : task.dueDate();
        String notes = task.notes() == null || task.notes().isBlank() ? "" : " · " + task.notes();
        return "Due " + dueDate + notes;
    }

    private static final class TaskCardTransferHandler extends TransferHandler {
        private final Task task;

        private TaskCardTransferHandler(Task task) {
            this.task = task;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return new StringSelection(String.valueOf(task.id()));
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }
    }
}
