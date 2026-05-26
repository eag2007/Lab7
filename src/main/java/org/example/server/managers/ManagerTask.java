package org.example.server.managers;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ManagerTask {
    public enum TaskStatus {
        PENDING,
        IN_PROGRESS,
        DONE,
        ERROR
    }

    public static class TaskInfo {
        private final String taskId;
        private final String commandName;
        private volatile TaskStatus status;
        private volatile String message;
        private volatile long created;
        private volatile long finished;

        public TaskInfo(String taskId, String commandName) {
            this.taskId = taskId;
            this.commandName = commandName;
            this.status = TaskStatus.PENDING;
            this.message = "Задача поставлена в очередь";
            this.created = System.currentTimeMillis();
            this.finished = -1;
        }

        public String getTaskId()      { return taskId; }
        public String getCommandName() { return commandName; }
        public TaskStatus getStatus()  { return status; }
        public String getMessage()     { return message; }
        public long getCreated()     { return created; }
        public long getFinished()    { return finished; }

        public void setStatus(TaskStatus status) { this.status = status; }
        public void setMessage(String message)   { this.message = message; }
        public void finish(String msg)  {
            this.status = TaskStatus.DONE;
            this.message = msg;
            this.finished = System.currentTimeMillis();
        }
        public void error(String msg)   {
            this.status = TaskStatus.ERROR;
            this.message = msg;
            this.finished = System.currentTimeMillis();
        }
    }

    private static final ConcurrentHashMap<String, TaskInfo> tasks = new ConcurrentHashMap<>();

    public static String createTask(String commandName) {
        String taskId = UUID.randomUUID().toString().substring(0, 8);
        tasks.put(taskId, new TaskInfo(taskId, commandName));
        return taskId;
    }

    public static TaskInfo getTask(String taskId) {
        return tasks.get(taskId);
    }

    public static boolean taskExists(String taskId) {
        return tasks.containsKey(taskId);
    }
}