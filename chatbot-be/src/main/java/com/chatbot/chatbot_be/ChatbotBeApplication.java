package com.chatbot.chatbot_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableFeignClients(basePackages = "com.chatbot.chatbot_be.proxy")
public class ChatbotBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatbotBeApplication.class, args);
	}

}
