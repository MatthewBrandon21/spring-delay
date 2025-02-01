package com.example.DelayProcessing;

import com.example.DelayProcessing.service.DelayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class DelayProcessingApplication {
	@Autowired
	DelayService delayService;

	public static void main(String[] args) {
		SpringApplication.run(DelayProcessingApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		delayService.loadTasks();
	}
}
