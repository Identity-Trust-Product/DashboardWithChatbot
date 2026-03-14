package com.chatbot.chatbot_be.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.chatbot.chatbot_be.service.ChatBotService;

@RestController
public class ChatBotController {
	
	@Autowired
	private ChatBotService chatBotService;
	
//	@GetMapping("/ask-quetion-and-get-query")
//	public String askQuestionAndGetQuery(@RequestParam String question) {
//		System.out.println("Question:"+question);
//		String response = chatBotService.askQuestionAndGetQuery(question);
//		return response;
//	}
	
@GetMapping("/check-service")
	public String serviceCheck() {
		return "Hello";
	}

	@GetMapping("/get-data-in-properties-file-servicewise/{moduleID}")
	public Set<String> askSerivceWiseDataInFile(@PathVariable("moduleID") String moduleID) {
		System.out.println("moduleID:"+moduleID);
		Set<String> response = chatBotService.askSerivceWiseDataInFile(moduleID);
		return response;
	}
	
	@GetMapping("/get-selected-question-answer/{question}")
	public Map<String, String> getSelectedQuestionsAnswer(@PathVariable("question") String question) {
		System.out.println("Question:"+question);
		Map<String, String> response = chatBotService.getSelectedQuestionsAnswer(question);
		return response;
	}
	
	@GetMapping("/get-answer-by-ask-quetion-using-rag/{que}")
	public  Map<String, String> getAnswerByAskQuetionUsingRAG(@PathVariable("que") String que) {
		System.out.println("RAG Question:"+que);
		 Map<String, String> response = chatBotService.getAnswerByAskQuetionUsingRAG(que);
		return response;
	}
	
	@PostMapping("/generate-answer-using-ollama")
	public List<Map<String, Object>> generateAnswerUsingOllama
		(@RequestParam String que, @RequestBody List<Map<String, Object>> dbLevelAnswer){
		System.err.println("============generateAnswerUsingOllama============");
		List<Map<String, Object>> list=chatBotService.generateAnswerUsingOllama(que,dbLevelAnswer);
		System.err.println("============generateAnswerUsingOllama============"+list);
		return list;
	}
	
	@PostMapping("/generate-answer-using-python-and-ollama")
	public List<Map<String, Object>> generateAnswerUsingOllamaWithPython
		(@RequestParam String que, @RequestBody List<Map<String, Object>> dbLevelAnswer){
		System.err.println("============generateAnswerUsingOllamaWithPython============");
		List<Map<String, Object>> list=chatBotService.generateAnswerUsingOllamaWithPython(que,dbLevelAnswer);
		System.err.println("============generateAnswerUsingOllamaWithPython============"+list);
		return list;
	}

}
