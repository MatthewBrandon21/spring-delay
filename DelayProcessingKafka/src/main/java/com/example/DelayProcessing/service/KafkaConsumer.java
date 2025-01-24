package com.example.DelayProcessing.service;

import com.example.DelayProcessing.config.Constants;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.listener.KafkaBackoffException;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {
    @RetryableTopic(attempts = "1", include = KafkaBackoffException.class, dltStrategy = DltStrategy.NO_DLT)
    @KafkaListener(topics = Constants.TOPIC, groupId = Constants.CONSUMER_GROUP_NAME)
    public void handleOrders(String message)  {
        System.out.println("Consumed message from delay topic : " + message);

    }
}
