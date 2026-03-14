package com.chatbot.chatbot_be.proxy;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;


import com.chatbot.chatbot_be.model.ChatIn;
import com.chatbot.chatbot_be.model.ChatOut;

import org.springframework.web.bind.annotation.PostMapping;


@FeignClient(name = "ChatBotOllamaClient", url = "http://10.210.0.44:8976/")
public interface ChatBotOllamaClient {

	//@PostMapping("/generate-answer")
//	@PostMapping(value = "/generate-answer/", consumes = "application/json")
//	public  List<Map<String, Object>> generateAnswerUsingOllamaWithPython(@RequestParam String que, @RequestParam List<Map<String, Object>> dbLevelAnswer);

	@PostMapping(value = "/generate-answer", consumes = "application/json")
	public ChatOut generateAnswerUsingOllamaWithPython(@RequestBody ChatIn input);
}
