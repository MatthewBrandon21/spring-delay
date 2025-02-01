package com.example.DelayProcessing.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerPartitionPausingBackOffManager;
import org.springframework.kafka.listener.ContainerPausingBackOffHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaConsumerBackoffManager;
import org.springframework.kafka.listener.ListenerContainerPauseService;
import org.springframework.kafka.listener.ListenerContainerRegistry;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class KafkaConsumerConfig {
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(ConsumerFactory<Object, Object> consumerFactory,
                                                                                                 ListenerContainerRegistry registry,
                                                                                                 TaskScheduler scheduler) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        KafkaConsumerBackoffManager backOffManager = createBackOffManager(registry, scheduler);
        // Set auto ACK immediately after processing each record
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setContainerCustomizer(container -> {
            DelayedMessageListenerAdapter<Object, Object> delayedAdapter = wrapWithDelayedMessageListenerAdapter(backOffManager, container);

            // for this example delay-topic will delay 10 second, and main-topic will not delay
            // List all topic that need to be delayed here
            delayedAdapter.setDelayForTopic(Constants.DELAY_TOPIC, Duration.ofSeconds(10));

            // Default topic duration
            delayedAdapter.setDefaultDelay(Duration.of(Constants.DEFAULT_DELAY_DURATION, ChronoUnit.SECONDS));

            container.setupMessageListener(delayedAdapter);
        });
        return factory;
    }

    @Bean
    public TaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @SuppressWarnings("unchecked")
    private DelayedMessageListenerAdapter<Object, Object> wrapWithDelayedMessageListenerAdapter(KafkaConsumerBackoffManager backOffManager, ConcurrentMessageListenerContainer<Object, Object> container) {
        return new DelayedMessageListenerAdapter<>((MessageListener<Object, Object>) container.getContainerProperties().getMessageListener(), backOffManager, container.getListenerId());
    }

    private ContainerPartitionPausingBackOffManager createBackOffManager(ListenerContainerRegistry registry, TaskScheduler scheduler) {
        return new ContainerPartitionPausingBackOffManager(registry, new ContainerPausingBackOffHandler(new ListenerContainerPauseService(registry, scheduler)));
    }

    // Get produced timestamp metadata in nanoseconds
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule()).configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    }
}
