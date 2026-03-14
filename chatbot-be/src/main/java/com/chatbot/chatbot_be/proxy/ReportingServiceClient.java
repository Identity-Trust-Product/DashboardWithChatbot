package com.chatbot.chatbot_be.proxy;

import java.util.Set;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "erp-reporting-jasper-be", url = "http://localhost:11310")
public interface ReportingServiceClient {
	
	@RequestMapping("/get-selected-question-answer")
	public Set<String> getSelectedQuestionsAnswer(@RequestParam(required = true) String query );

}
