package com.example.todo;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

// Serializable request wrapper for the TCP client-server sync protocol.
public class TaskSyncRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String type;
    private final List<Task> tasks;

    public TaskSyncRequest(String type, List<Task> tasks) {
        this.type = type;
        this.tasks = tasks;
    }

    public String getType() {
        return type;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public static TaskSyncRequest pushTasks(List<Task> tasks) {
        return new TaskSyncRequest("PUSH_TASKS", tasks);
    }

    public static TaskSyncRequest getReportCsv() {
        return new TaskSyncRequest("GET_REPORT_CSV", null);
    }
}
