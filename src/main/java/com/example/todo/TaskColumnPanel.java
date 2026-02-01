package com.example.todo;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;

public class TaskColumnPanel extends JPanel {
    private final Color baseColor;
    private final Color highlightColor;

    public TaskColumnPanel(Color baseColor, Color highlightColor) {
        this.baseColor = baseColor;
        this.highlightColor = highlightColor;
        setBackground(baseColor);
        setOpaque(true);
        installDropTarget();
    }

    private void installDropTarget() {
        new DropTarget(this, DnDConstants.ACTION_MOVE, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                setHighlight(true);
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                setHighlight(false);
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                setHighlight(false);
            }
        }, true);
    }

    private void setHighlight(boolean highlight) {
        setBackground(highlight ? highlightColor : baseColor);
        repaint();
    }
}
