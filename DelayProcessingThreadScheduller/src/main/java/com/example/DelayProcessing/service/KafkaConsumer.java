package com.example.DelayProcessing.service;

import com.example.DelayProcessing.config.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    @Autowired
    DelayService delayService;

    @KafkaListener(topics = Constants.TOPIC, groupId = Constants.CONSUMER_GROUP_NAME)
    public void consumerMessageMainTopic(String message) {
        System.out.println("Consumed message from main topic : " + message);
    }

    @KafkaListener(topics = Constants.DELAY_TOPIC, groupId = Constants.CONSUMER_GROUP_NAME)
    public void consumerMessageDelayTopic(String message) {
        System.out.println("Consumed message from delay topic : " + message);
        delayService.scheduleTask(message, Constants.DELAY_VALUE);
    }
}
