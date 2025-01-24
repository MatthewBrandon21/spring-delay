package com.example.DelayProcessing.service;

import com.example.DelayProcessing.config.Constants;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

@Service
public class KafkaConsumer {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @KafkaListener(topics = Constants.TOPIC, groupId = Constants.CONSUMER_GROUP_NAME)
    public void consumerMessageMainTopic(String message) {
        System.out.println("Consumed message from main topic : " + message);
    }

    @KafkaListener(topics = Constants.DELAY_TOPIC, groupId = Constants.CONSUMER_GROUP_NAME)
    public void consumerMessageDelayTopic(String message) {
        //"key:delay:value"
        String[] parts = message.split(":");
        String key = parts[0];
        int delayInSeconds = Integer.parseInt(parts[1]);
        String value = parts[2];

        // Store in Redis with expiration
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(delayInSeconds));
        System.out.println("Added message to Redis with delay: " + delayInSeconds + " seconds");
    }
}
