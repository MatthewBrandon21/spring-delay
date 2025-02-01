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

    // Limit only 2 thread for pooling the task
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public void processTasks() {
        executorService.submit(() -> {
            while (true) {
                System.out.println("Pooling Service is running on :" + Thread.currentThread().getName());

                // Fetch the task with the earliest expiration time
                Set<String> tasks = redisTemplate.opsForZSet().range(Constants.MIN_HEAP_DELAY_RECORD_KEY, 0, -1);

                if (tasks == null || tasks.isEmpty()) {
                    // No tasks left, stop pooling
                    System.out.println("Task empty, exiting");
                    break;
                }

                System.out.println("Task pending count : " + (long) tasks.size());

                String task = tasks.iterator().next();
                String lockKey = Constants.LOCK_KEY_DELAY_RECORD_PREFIX + task;

                // Try to acquire a lock on the task
                if (redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(10))) {
                    try {
                        Double score = redisTemplate.opsForZSet().score(Constants.MIN_HEAP_DELAY_RECORD_KEY, task);
                        if (score != null && score <= System.currentTimeMillis()) {
                            // Task is expired, publish it and remove it from Redis
                            kafkaTemplate.send("main-topic", task);
                            redisTemplate.opsForZSet().remove(Constants.MIN_HEAP_DELAY_RECORD_KEY, task);
                            System.out.println("Processed expired task: " + task);
                        } else {
                            // Task is not expired, release the lock and sleep
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        // Release the lock
                        redisTemplate.delete(lockKey);
                    }
                } else {
                    try {
                        // Sleep little bit
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}
