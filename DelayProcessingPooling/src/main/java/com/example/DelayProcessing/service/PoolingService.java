package com.example.DelayProcessing.service;

import com.example.DelayProcessing.config.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class PoolingService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public void processTasks() {
        executorService.submit(() -> {
            while (true) {
                System.out.println("Pooling Running");
                // Fetch the task with the earliest expiration time
                Set<String> tasks = redisTemplate.opsForZSet().rangeByScore(Constants.MIN_HEAP_KEY, 0,
                        System.currentTimeMillis(), 0, 1);
                if (tasks == null || tasks.isEmpty()) {
                    System.out.println(tasks);
                    System.out.println("exiting");
                    break; // No tasks left
                }

                String task = tasks.iterator().next();
                String lockKey = Constants.LOCK_KEY_PREFIX + task;

                // Try to acquire a lock on the task
                if (redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(10))) {
                    try {
                        Double score = redisTemplate.opsForZSet().score(Constants.MIN_HEAP_KEY, task);
                        if (score != null && score <= System.currentTimeMillis()) {
                            // Task is expired, publish it and remove it from Redis
                            kafkaTemplate.send("main-topic", task);
                            redisTemplate.opsForZSet().remove(Constants.MIN_HEAP_KEY, task);
                            System.out.println("Processed expired task: " + task);
                        } else {
                            // Task is not expired, release the lock and sleep
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        redisTemplate.delete(lockKey); // Release the lock
                    }
                } else {
                    try {
                        Thread.sleep(1000); // Wait before trying again
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}
