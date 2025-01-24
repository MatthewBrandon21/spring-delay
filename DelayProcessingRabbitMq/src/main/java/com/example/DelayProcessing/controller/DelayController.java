package com.example.DelayProcessing.controller;

import com.example.DelayProcessing.model.Request;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/delay")
public class DelayController {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/publish/delay")
    public String publishMessaseToDelayTopic(@RequestBody Request request) {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("x-delay", request.getDelay());
        Message message = new Message(request.getMessage().getBytes(), messageProperties);

        rabbitTemplate.send("delayed-exchange", "delayed-queue", message);
        return "Message published with delay: " + request.getDelay() + " ms";
    }
}
