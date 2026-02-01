package com.example.todo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class TaskSyncClient {
    public static final int DEFAULT_PORT = 5555;

    private final String host;
    private final int port;

    public TaskSyncClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String syncTasks(List<Task> tasks) throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket(host, port)) {
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush();
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            outputStream.writeObject(tasks);
            outputStream.flush();

            Object response = inputStream.readObject();
            if (response instanceof String message) {
                return message;
            }
            return "Sync completed successfully";
        }
    }
}
