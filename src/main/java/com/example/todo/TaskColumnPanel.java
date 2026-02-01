package com.example.todo;

import javax.swing.JPanel;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.IOException;

public class TaskColumnPanel extends JPanel {
    private static final DataFlavor TASK_FLAVOR;

    static {
        try {
            TASK_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + Task.class.getName());
        } catch (ClassNotFoundException error) {
            throw new ExceptionInInitializerError(error);
        }
    }

    private final TaskStatus status;

    public TaskColumnPanel(TaskStatus status) {
        this.status = status;
        new DropTarget(this, DnDConstants.ACTION_MOVE, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent event) {
                if (isTaskFlavor(event)) {
                    event.acceptDrag(DnDConstants.ACTION_MOVE);
                } else {
                    event.rejectDrag();
                }
            }

            @Override
            public void dragOver(DropTargetDragEvent event) {
                if (isTaskFlavor(event)) {
                    event.acceptDrag(DnDConstants.ACTION_MOVE);
                } else {
                    event.rejectDrag();
                }
            }

            @Override
            public void dropActionChanged(DropTargetDragEvent event) {
                if (isTaskFlavor(event)) {
                    event.acceptDrag(DnDConstants.ACTION_MOVE);
                } else {
                    event.rejectDrag();
                }
            }

            @Override
            public void dragExit(DropTargetEvent event) {
                // No-op, but required by DropTargetListener.
            }

            @Override
            public void drop(DropTargetDropEvent event) {
                if (!isTaskFlavor(event)) {
                    event.rejectDrop();
                    return;
                }

                event.acceptDrop(DnDConstants.ACTION_MOVE);
                try {
                    Transferable transferable = event.getTransferable();
                    Task task = (Task) transferable.getTransferData(TASK_FLAVOR);
                    task.setStatus(status);
                    event.dropComplete(true);
                } catch (UnsupportedOperationException | IOException | java.awt.datatransfer.UnsupportedFlavorException error) {
                    event.dropComplete(false);
                }
            }

            private boolean isTaskFlavor(DropTargetDragEvent event) {
                return event.isDataFlavorSupported(TASK_FLAVOR);
            }

            private boolean isTaskFlavor(DropTargetDropEvent event) {
                return event.isDataFlavorSupported(TASK_FLAVOR);
            }
        }, true);
    }

    public TaskStatus getStatus() {
        return status;
    }
}
