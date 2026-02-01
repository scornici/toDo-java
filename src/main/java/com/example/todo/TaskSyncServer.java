package com.example.todo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskSyncServer {
    public static final int DEFAULT_PORT = 5555;

    private final int port;
    private final List<Task> lastReceivedTasks = new ArrayList<>();

    public TaskSyncServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.printf("Task sync server listening on port %d.%n", port);
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleClient(clientSocket);
                } catch (ClassNotFoundException ex) {
                    System.err.println("Failed to read tasks from client: " + ex.getMessage());
                }
            }
        }
    }

    public List<Task> getLastReceivedTasks() {
        return Collections.unmodifiableList(lastReceivedTasks);
    }

    private void handleClient(Socket clientSocket) throws IOException, ClassNotFoundException {
        ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        outputStream.flush();
        ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());

        Object payload = inputStream.readObject();
        TaskSyncResponse response = handleRequest(payload);

        outputStream.writeObject(response);
        outputStream.flush();
    }

    // Request-response handling for push and report download.
    private TaskSyncResponse handleRequest(Object payload) {
        if (!(payload instanceof TaskSyncRequest request)) {
            return new TaskSyncResponse(false, "Invalid request received.", null);
        }

        if ("PUSH_TASKS".equals(request.getType())) {
            List<Task> tasks = request.getTasks();
            lastReceivedTasks.clear();
            if (tasks != null) {
                lastReceivedTasks.addAll(tasks);
            }

            System.out.printf("Received %d task(s).%n", lastReceivedTasks.size());
            for (Task task : lastReceivedTasks) {
                System.out.printf("- [%s] %s (due: %s)%n", task.status(), task.title(), task.dueDate());
            }

            return new TaskSyncResponse(true, "Sync completed. Received " + lastReceivedTasks.size() + " tasks.", null);
        }

        if ("GET_REPORT_CSV".equals(request.getType())) {
            if (lastReceivedTasks.isEmpty()) {
                return new TaskSyncResponse(false, "No tasks synced yet.", null);
            }
            String csv = buildCsvReport(lastReceivedTasks);
            return new TaskSyncResponse(true, "Report generated.", csv);
        }

        return new TaskSyncResponse(false, "Unknown request type.", null);
    }

    // Report generation for the last synchronized tasks.
    private String buildCsvReport(List<Task> tasks) {
        int total = tasks.size();
        int todo = 0;
        int doing = 0;
        int done = 0;
        for (Task task : tasks) {
            if (task.status() == TaskStatus.TODO) {
                todo++;
            } else if (task.status() == TaskStatus.DOING) {
                doing++;
            } else if (task.status() == TaskStatus.DONE) {
                done++;
            }
        }

        double completionRate = total == 0 ? 0.0 : (done * 100.0) / total;

        StringBuilder builder = new StringBuilder();
        builder.append("total_tasks,todo_count,doing_count,done_count,completion_rate_percent\n");
        builder.append(total)
            .append(',')
            .append(todo)
            .append(',')
            .append(doing)
            .append(',')
            .append(done)
            .append(',')
            .append(String.format("%.2f", completionRate))
            .append("\n\n");

        builder.append("id,title,status,dueDate\n");
        for (Task task : tasks) {
            builder.append(task.id())
                .append(',')
                .append(escapeCsv(task.title()))
                .append(',')
                .append(task.status())
                .append(',')
                .append(task.dueDate() == null ? "" : task.dueDate())
                .append('\n');
        }

        return builder.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    public static void main(String[] args) throws IOException {
        new TaskSyncServer(DEFAULT_PORT).start();
    }
}
