package com.example.todo;


import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.List;
import java.util.function.BiConsumer;


public class TaskColumnPanel extends JPanel {
    private static final Color COLUMN_BORDER = new Color(220, 223, 232);
    private static final Color COLUMN_HIGHLIGHT = new Color(230, 233, 248);

    private final TaskStatus status;
    private final JPanel cardContainer;
    private final BiConsumer<Integer, TaskStatus> onDrop;

    public TaskColumnPanel(String title, TaskStatus status, BiConsumer<Integer, TaskStatus> onDrop) {
        this.status = status;
        this.onDrop = onDrop;
        setLayout(new BorderLayout());
        setBackground(ToDoApp.BACKGROUND);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel header = new JLabel(title);
        header.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        header.setForeground(ToDoApp.TEXT_PRIMARY);
        header.setBorder(new EmptyBorder(8, 8, 8, 8));
        add(header, BorderLayout.NORTH);

        cardContainer = new JPanel();
        cardContainer.setLayout(new javax.swing.BoxLayout(cardContainer, javax.swing.BoxLayout.Y_AXIS));
        cardContainer.setBackground(ToDoApp.BACKGROUND);
        cardContainer.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(cardContainer);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLUMN_BORDER));
        scrollPane.setBackground(ToDoApp.BACKGROUND);
        scrollPane.getViewport().setBackground(ToDoApp.BACKGROUND);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(240, 420));
        add(scrollPane, BorderLayout.CENTER);

        cardContainer.setTransferHandler(new ColumnDropHandler());
        cardContainer.addPropertyChangeListener("dropLocation", event -> {
            if (event.getNewValue() == null) {
                setHighlighted(false);
            }
        });

    }

    public TaskStatus status() {
        return status;
    }

    public void setTasks(List<Task> tasks, java.util.function.Consumer<TaskCard> onSelect) {
        cardContainer.removeAll();
        if (tasks.isEmpty()) {
            JLabel empty = new JLabel("No tasks");
            empty.setForeground(ToDoApp.TEXT_SECONDARY);
            empty.setBorder(new EmptyBorder(8, 4, 8, 4));
            cardContainer.add(empty);
        } else {
            for (Task task : tasks) {
                TaskCard card = new TaskCard(task, onSelect);
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                cardContainer.add(card);
                cardContainer.add(javax.swing.Box.createVerticalStrut(8));
            }
        }
        revalidate();
        repaint();
    }

    private void setHighlighted(boolean highlighted) {
        Color color = highlighted ? COLUMN_HIGHLIGHT : ToDoApp.BACKGROUND;
        cardContainer.setBackground(color);
        repaint();
    }

    private final class ColumnDropHandler extends TransferHandler {
        @Override
        public boolean canImport(TransferSupport support) {
            boolean supported = support.isDataFlavorSupported(DataFlavor.stringFlavor);
            setHighlighted(supported);
            return supported;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            try {
                Transferable transferable = support.getTransferable();
                String value = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                int taskId = Integer.parseInt(value);
                onDrop.accept(taskId, status);
                return true;
            } catch (Exception ex) {
                return false;
            } finally {
                setHighlighted(false);
            }
        }
    }
}
