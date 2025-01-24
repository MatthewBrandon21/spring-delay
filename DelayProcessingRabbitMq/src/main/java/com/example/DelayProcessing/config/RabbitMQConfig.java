package com.example.DelayProcessing.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitMQConfig {
    @Bean
    public CustomExchange delayedExchange() {
        return new CustomExchange(Constants.DELAYED_EXCHANGE, "x-delayed-message", true, false, Map.of("x-delayed-type", "direct"));
    }

    @Bean
    public Queue delayedQueue() {
        return new Queue(Constants.QUEUE_NAME, true, false, false);
    }

    @Bean
    public Binding binding(Queue delayedQueue, CustomExchange delayedExchange) {
        return BindingBuilder.bind(delayedQueue).to(delayedExchange).with(Constants.QUEUE_NAME).noargs();
    }
}
