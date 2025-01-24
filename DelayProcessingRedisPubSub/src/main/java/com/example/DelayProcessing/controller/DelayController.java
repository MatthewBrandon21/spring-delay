package com.example.DelayProcessing.controller;

import com.example.DelayProcessing.service.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/delay")
public class DelayController {
    @Autowired
    private KafkaProducer kafkaProducer;

    @PostMapping("/publish/main")
    public String publishMessaseToMainTopic(@RequestParam String message) {
        kafkaProducer.sendMessageToMainTopic(message);
        return "Message sent to main topic kafka: " + message;
    }

    @PostMapping("/publish/delay")
    public String publishMessaseToDelayTopic(@RequestParam String message) {
        kafkaProducer.sendMessageToDelayTopic(message);
        return "Message sent to delay topic kafka: " + message;
    }
}
