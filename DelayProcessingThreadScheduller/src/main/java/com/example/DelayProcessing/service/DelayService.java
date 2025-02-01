package com.example.DelayProcessing.service;

import com.example.DelayProcessing.config.Constants;
import com.example.DelayProcessing.model.ScheduledTask;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Service
public class DelayService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ThreadPoolTaskScheduler taskScheduler;
    private final List<ScheduledTask> pendingTasks = new ArrayList<>();
    private static final String TASKS_FILE = "tasks.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DelayService(ThreadPoolTaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    public void scheduleTask(String data, long delay) {
        long executeAt = Instant.now().getEpochSecond() + delay;
        ScheduledTask task = new ScheduledTask(data, executeAt);
        pendingTasks.add(task);
        saveTasks();

        ScheduledFuture<?> future = taskScheduler.schedule(() -> executeTask(task), Instant.ofEpochSecond(executeAt));
        System.out.println("Task scheduled: " + data);
    }

    public void executeTask(ScheduledTask task) {
        System.out.println("Executing task: " + task.taskName);
        kafkaTemplate.send(Constants.TOPIC, task.taskName);
        pendingTasks.remove(task);
        saveTasks();
    }

    public void saveTasks() {
        try {
            objectMapper.writeValue(new File(TASKS_FILE), pendingTasks);
        } catch (Exception e) {
            System.out.println("Error while saving task, Error : " + e.getMessage());
        }
    }

    public void loadTasks() {
        try {
            if (Files.exists(Paths.get(TASKS_FILE))) {
                List<ScheduledTask> tasks = objectMapper.readValue(new File(TASKS_FILE), new TypeReference<>() {});
                for (ScheduledTask task : tasks) {
                    System.out.println(task);
                    if (task.getExecuteAt() > Instant.now().getEpochSecond()) {
                        scheduleTask(task.getTaskName(), task.getExecuteAt() - Instant.now().getEpochSecond());
                    } else {
                        executeTask(task);
                    }
                }
            } else {
                System.out.println("Task file not found");
            }
        } catch (Exception e) {
            System.out.println("Error while load tasks, Error :" + e.getMessage());
        }
    }
}
