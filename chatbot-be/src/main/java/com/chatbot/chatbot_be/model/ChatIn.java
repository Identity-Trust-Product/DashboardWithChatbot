package com.chatbot.chatbot_be.model;

import java.util.List;
import java.util.Map;

public class ChatIn {

	 private String question;
	    private List<Map<String, Object>> db_data;

	    public ChatIn() {}

	    public ChatIn(String question, List<Map<String, Object>> db_data) {
	        this.question = question;
	        this.db_data = db_data;
	    }

	    public String getQuestion() {
	        return question;
	    }

	    public void setQuestion(String question) {
	        this.question = question;
	    }

	    public List<Map<String, Object>> getDb_data() {
	        return db_data;
	    }

	    public void setDb_data(List<Map<String, Object>> db_data) {
	        this.db_data = db_data;
	    }
}
