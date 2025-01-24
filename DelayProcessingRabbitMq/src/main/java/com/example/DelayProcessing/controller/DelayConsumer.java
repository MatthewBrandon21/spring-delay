package com.example.DelayProcessing.controller;

import com.example.DelayProcessing.config.Constants;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DelayConsumer {
    @RabbitListener(queues = Constants.QUEUE_NAME)
    public void receiveMessage(String message) {
        System.out.println("Received message: " + message);
    }
}
