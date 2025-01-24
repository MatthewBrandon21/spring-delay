package com.example.DelayProcessing.service;

import com.example.DelayProcessing.config.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    @Autowired
    private PoolingService poolingService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @KafkaListener(topics = Constants.TOPIC, groupId = Constants.CONSUMER_GROUP_NAME)
    public void consumerMessageMainTopic(String message) {
        System.out.println("Consumed message from main topic : " + message);
    }

    @KafkaListener(topics = Constants.DELAY_TOPIC, groupId = Constants.CONSUMER_GROUP_NAME)
    public void consumerMessageDelayTopic(String message) {
        System.out.println("Consumed message from delay topic : " + message);
        // Add task to Redis min-heap with 15-minute delay
        long expirationTime = System.currentTimeMillis() + 15 * 1000;
        redisTemplate.opsForZSet().add(Constants.MIN_HEAP_KEY, message, expirationTime);

        // Trigger task processing
        poolingService.processTasks();
    }
}
