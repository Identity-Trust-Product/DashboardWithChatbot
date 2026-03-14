package com.chatbot.chatbot_be.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ChatBotService {

	
	//String askQuestionAndGetQuery(String question);
	Set<String> askSerivceWiseDataInFile(String serviceName);
	List<Map<String, Object>> getSelectedQuestionsAnswer(String questions);
	 List<Map<String, Object>> getAnswerByAskQuetionUsingRAG(String que);
	 List<Map<String, Object>> generateAnswerUsingOllama(String que,List<Map<String, Object>> dbLevelAnswer);
	 List<Map<String, Object>> generateAnswerUsingOllamaWithPython(String que,List<Map<String, Object>> dbLevelAnswer);

}
