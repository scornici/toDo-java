package com.example.todo;

import java.io.Serial;
import java.io.Serializable;

// Serializable response wrapper for the TCP client-server sync protocol.
public class TaskSyncResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final boolean ok;
    private final String message;
    private final String csv;

    public TaskSyncResponse(boolean ok, String message, String csv) {
        this.ok = ok;
        this.message = message;
        this.csv = csv;
    }

    public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
    }

    public String getCsv() {
        return csv;
    }
}
