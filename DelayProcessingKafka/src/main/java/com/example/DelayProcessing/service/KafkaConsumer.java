package com.example.DelayProcessing.service;

import com.example.DelayProcessing.config.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.KafkaBackoffException;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @RetryableTopic(attempts = "1", include = KafkaBackoffException.class, dltStrategy = DltStrategy.NO_DLT)
    @KafkaListener(topics = Constants.DELAY_TOPIC, groupId = Constants.CONSUMER_GROUP_NAME)
    public void handleDelayedProcess(String message)  {
        System.out.println("Consumed message from delay topic : " + message);

        // Put to main topic
        kafkaTemplate.send(Constants.TOPIC, message);
    }

    @KafkaListener(topics = Constants.TOPIC, groupId = Constants.CONSUMER_GROUP_NAME)
    public void handleMainTopic(String message)  {
        System.out.println("Consumed message from main topic : " + message);
    }
}
