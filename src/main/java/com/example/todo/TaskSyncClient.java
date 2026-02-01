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
        TaskSyncResponse response = sendRequest(TaskSyncRequest.pushTasks(tasks));
        if (response.isOk()) {
            return response.getMessage();
        }
        throw new IOException(response.getMessage());
    }

    // Request-response interaction over TCP for sync operations.
    public String downloadReportCsv() throws IOException, ClassNotFoundException {
        TaskSyncResponse response = sendRequest(TaskSyncRequest.getReportCsv());
        if (response.isOk()) {
            return response.getCsv();
        }
        throw new IOException(response.getMessage());
    }

    private TaskSyncResponse sendRequest(TaskSyncRequest request) throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket(host, port)) {
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush();
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            outputStream.writeObject(request);
            outputStream.flush();

            Object response = inputStream.readObject();
            if (response instanceof TaskSyncResponse typedResponse) {
                return typedResponse;
            }
            throw new IOException("Unexpected response from sync server.");
        }
    }
}
