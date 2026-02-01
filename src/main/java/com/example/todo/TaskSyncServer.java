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
        if (payload instanceof List<?> list) {
            lastReceivedTasks.clear();
            for (Object item : list) {
                if (item instanceof Task task) {
                    lastReceivedTasks.add(task);
                }
            }
        }

        System.out.printf("Received %d task(s).%n", lastReceivedTasks.size());
        for (Task task : lastReceivedTasks) {
            System.out.printf("- [%s] %s (due: %s)%n", task.status(), task.title(), task.dueDate());
        }

        outputStream.writeObject("Sync completed. Received " + lastReceivedTasks.size() + " tasks.");
        outputStream.flush();
    }

    public static void main(String[] args) throws IOException {
        new TaskSyncServer(DEFAULT_PORT).start();
    }
}
