package com.example.DelayProcessing.service;

import com.example.DelayProcessing.config.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessageToMainTopic(String message) {
        kafkaTemplate.send(Constants.TOPIC, message);
    }

    public void sendMessageToDelayTopic(String message) {
        kafkaTemplate.send(Constants.DELAY_TOPIC, message);
    }
}
