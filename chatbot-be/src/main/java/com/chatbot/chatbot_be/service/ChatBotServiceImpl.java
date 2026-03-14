package com.chatbot.chatbot_be.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;


import com.chatbot.chatbot_be.model.ChatIn;
import com.chatbot.chatbot_be.model.ChatOut;
import com.chatbot.chatbot_be.proxy.ChatBotOllamaClient;



@Service
public class ChatBotServiceImpl implements ChatBotService {

    private final PropertiesLoaderService propertiesLoaderService;
    private final EmbeddingModel embeddingModel;
    private final OllamaChatModel ollamaChatModel;
    private final ChatBotOllamaClient chatBotOllamaClient;

    public ChatBotServiceImpl(PropertiesLoaderService propertiesLoaderService, EmbeddingModel embeddingModel,OllamaChatModel ollamaChatModel,ChatBotOllamaClient chatBotOllamaClient) {
        this.propertiesLoaderService = propertiesLoaderService;
        this.embeddingModel = embeddingModel;
        this.ollamaChatModel = ollamaChatModel;
        this.chatBotOllamaClient = chatBotOllamaClient;
    }
    
    @Value("${spring.ai.ollama.chat.model}")
    private String currentModel;
    
//	@Override
//	public String askQuestionAndGetQuery(String userQuestion) {
//		
//		 String closestQuestion = null;
//	        int minDistance = Integer.MAX_VALUE;
//	        for (String key : faqMap.keySet()) {
//	            int dist = distance.apply(userQuestion.toLowerCase(), key.toLowerCase());
//	            if (dist < minDistance) {
//	                minDistance = dist;
//	                closestQuestion = key;
//	            }
//	        }
//	        if (closestQuestion != null && minDistance <= 10) { 
//	            return faqMap.get(closestQuestion);
//	        }
//
//	        return "Sorry, I don't have an answer for that.";
//	}

	@Override
	public Set<String> askSerivceWiseDataInFile(String serviceName) {
		Properties props=getProperty(serviceName);
		
		Set<String> keys = props.keySet().stream()
		        .map(Object::toString) 
		        .collect(Collectors.toSet());
		
		return keys;
	}
	

	 
	 
	 @Override
		public Map<String, String> getSelectedQuestionsAnswer(String question) {
		 String serviceName = question.split("\\.")[0];  
		 System.out.println(serviceName);
			Properties props=getProperty(serviceName);
			Map<String, String> questionAndQuery=  props.entrySet().stream()
	                    .collect(Collectors.toMap(
	                    	e -> String.valueOf(e.getKey()).replaceFirst("^" + serviceName+".questions.", ""),
	                        e -> String.valueOf(e.getValue())
	                    ));
			
	        String onlyQuestion = question.substring(question.lastIndexOf(".") + 1);
	        String query = questionAndQuery.get(onlyQuestion);
			if (query != null) {
			    System.out.println("Matched Query: " + query);
			} else {
			    System.out.println("No match found for key: " + question);
			}
			 Map<String, String> keys = new HashMap<>();
		        keys.put(serviceName, query);
		        return keys;
		}
	 
	 public Properties getProperty(String serviceName) {
	        try {
	            Properties props = new Properties();
	            props.load(new ClassPathResource(serviceName + ".properties").getInputStream());
	           // return props.getProperty(key);
	            return props;
	        } catch (IOException e) {
	            throw new RuntimeException("Could not load property file: " + serviceName, e);
	        }
	    }

	@Override
	public  Map<String, String> getAnswerByAskQuetionUsingRAG(String que) {
		Map<String, String> data = propertiesLoaderService.getAll();
        float[] userEmbedding = embeddingModel.embed(que);
        String bestKey = null;
        String FinalServiceNameAndQuetion=null;
        double bestScore = -1;
        for (String key : data.keySet()) {
        	String ServiceNameAndQuetion=key;
            String cleanQuestion = key.replace("^[^.]+\\.questions\\.", "").replace("-", " ").trim();

            float[] storedEmbedding = embeddingModel.embed(cleanQuestion);
            double score = cosineSimilarity(userEmbedding, storedEmbedding);
	            if (score > bestScore) {
	                bestScore = score;
	                bestKey = key;
	                FinalServiceNameAndQuetion=ServiceNameAndQuetion.split("\\.")[0];
	            }
	        }
        Map<String, String> keys = new HashMap<>();
        keys.put(FinalServiceNameAndQuetion, data.get(bestKey));
        return keys;
	}

	 private double cosineSimilarity(float[] v1, float[] v2) {
		 double dot = 0.0, normA = 0.0, normB = 0.0;
	        int len = Math.min(v1.length, v2.length);
	        for (int i = 0; i < len; i++) {
	            dot += v1[i] * v2[i];
	            normA += v1[i] * v1[i];
	            normB += v2[i] * v2[i];
	        }
	        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
	    }


	 
	//31 october 2025 
//	 @Override
//	 public List<Map<String, Object>> generateAnswerUsingOllama(String que, List<Map<String, Object>> dbLevelAnswer) {
//
//	     StringBuilder prompt = new StringBuilder();
//	     
//	     // System role and instructions
//	     prompt.append("You are an enterprise data assistant. Your role is to translate database query results into clear, natural language answers.\n\n");
//	     
//	     prompt.append("Guidelines:\n");
//	     prompt.append("- Provide direct, conversational responses\n");
//	     prompt.append("- Use actual names, values, and specifics from the data\n");
//	     prompt.append("- For single results: give a complete sentence\n");
//	     prompt.append("- For multiple results: summarize or list key points\n");
//	     prompt.append("- For counts/numbers: explain what they represent\n");
//	     prompt.append("- Format your response with line breaks for readability\n");
//	     prompt.append("- Use bold (**text**) for important values like names, IDs, or counts\n");
//	     prompt.append("- Never show raw SQL, JSON, or technical data structures\n");
//	     prompt.append("- If no data exists, clearly state that\n\n");
//	     
//	     // User question
//	     prompt.append("User Question:\n");
//	     prompt.append(que).append("\n\n");
//	     
//	     // Database results
//	     prompt.append("Query Results:\n");
//	     if (dbLevelAnswer == null || dbLevelAnswer.isEmpty()) {
//	         prompt.append("No matching records found.\n");
//	     } else {
//	         for (int i = 0; i < dbLevelAnswer.size(); i++) {
//	             Map<String, Object> row = dbLevelAnswer.get(i);
//	             prompt.append("Record ").append(i + 1).append(": ");
//	             prompt.append(row.toString()).append("\n");
//	         }
//	     }
//	     
//	     prompt.append("\nProvide a natural language answer based on the above data:");
//
//	     String aiResponse = ollamaChatModel.call(prompt.toString());
//
//	     Map<String, Object> responseMap = new LinkedHashMap<>();
//	     responseMap.put("", aiResponse.trim());
//
//	     return List.of(responseMap);
//	 }
	 
	 @Override
	 public List<Map<String, Object>> generateAnswerUsingOllama(String que, List<Map<String, Object>> dbLevelAnswer) {

	     StringBuilder prompt = new StringBuilder();

	     prompt.append("You are an enterprise data assistant. Your role is to translate database query results into clear, natural language answers.\n\n");
	     prompt.append("Guidelines:\n");
	     prompt.append("- DO NOT say: 'Sure', 'Here is the answer', 'Based on query results', or similar phrases.\n");
	     prompt.append("- Provide direct, conversational responses\n");
	     prompt.append("- DO NOT repeat the question.\n");
	     prompt.append("- Use actual names, values, and specifics from the data\n");
	     prompt.append("- For single results: give a complete sentence\n");
	     prompt.append("- For multiple results: summarize or list key points\n");
	     prompt.append("- For counts/numbers: explain what they represent\n");
	     prompt.append("- Format your response with line breaks for readability\n");
	     prompt.append("- Use ONLY HTML bold tags like <b>value</b>. \n");
	     prompt.append("- Do NOT use ** or any markdown bold format.Use bold (<b>text</b>) for important values like names, IDs, or counts\n");
	     prompt.append("- Never show raw SQL, JSON, or technical data structures\n");
	     prompt.append("- If no data exists, clearly state that\n\n");

	     prompt.append("Employee question:\n");
	     prompt.append(que).append("\n\n");

	     prompt.append("Database Records:\n");
	     if (dbLevelAnswer == null || dbLevelAnswer.isEmpty()) {
	         prompt.append("None\n\n");
	     } else {
	         for (int i = 0; i < dbLevelAnswer.size(); i++) {
	             prompt.append(dbLevelAnswer.get(i).toString()).append("\n");
	         }
	     }

	     prompt.append("\nGenerate ONLY the final clean answer for the employee. ");
	     prompt.append("Do not add any introduction or explanation. ");
	     prompt.append("Start directly with the information:\n");
	     prompt.append("FINAL ANSWER:\n");

	     String aiResponse = ollamaChatModel.call(prompt.toString());
	     System.out.println("🔍 Using Ollama Model: "+ currentModel);

	     Map<String, Object> responseMap = new LinkedHashMap<>();
	     responseMap.put("", aiResponse.trim());

	     return List.of(responseMap);
	 }

	 
// tabular format
//	 @Override
//	 public List<Map<String, Object>> generateAnswerUsingOllama(String que, List<Map<String, Object>> dbLevelAnswer) {
//
//	     StringBuilder prompt = new StringBuilder();
//
//	     prompt.append("""
//	         You are an enterprise business assistant that explains results professionally.
//	         Your ONLY task is to present the given structured data below in clean, valid HTML — no markdown, no code fences.
//
//	         STRICT RULES:
//	         1️⃣ Never invent or assume any data not explicitly shown in the context.
//	         2️⃣ If data is empty or unrelated to the user’s question, say clearly:
//	            "<p>No relevant information was found.</p>"
//	         3️⃣ NEVER restate the question or include explanations like “Here’s your result.”
//	         4️⃣ Generate ONLY pure HTML (<p>, <div>, <table>).
//	         5️⃣ Use the following:
//	            - 📋 for listing data
//	            - ✅ for approvals
//	            - ❌ for denials
//	            - 📍 for locations
//	            - 📞 for phone numbers
//	         6️⃣ For multiple records, use:
//	            <table style='width:100%; border-collapse:collapse; font-size:14px; margin:15px 0;'>
//	              <thead style='background:linear-gradient(135deg,#667eea 0%,#764ba2 100%); color:white;'>
//	                <tr><th>Column1</th><th>Column2</th>...</tr>
//	              </thead>
//	              <tbody>...</tbody>
//	            </table>
//	         7️⃣ For one record, use:
//	            <div style='background:#f8f9fa; padding:15px; border-radius:10px; border-left:4px solid #667eea; margin:10px 0;'>
//	              <strong>Label:</strong> Value<br>
//	            </div>
//	         8️⃣ Avoid repeating the same record multiple times.
//	         9️⃣ Skip empty/null columns.
//	         🔟 Summarize at the top, like "📋 Found 3 records:".
//	         """);
//
//	     prompt.append("\n\nUSER QUESTION:\n").append(que).append("\n");
//
//	     int recordCount = (dbLevelAnswer == null) ? 0 : dbLevelAnswer.size();
//	     prompt.append("\nDATASET (structured list):\n");
//	     if (recordCount == 0) {
//	         prompt.append("No records found.\n");
//	     } else {
//	         for (int i = 0; i < Math.min(recordCount, 5); i++) {
//	             Map<String, Object> record = dbLevelAnswer.get(i);
//	             prompt.append("- Record ").append(i + 1).append(":\n");
//	             for (Map.Entry<String, Object> e : record.entrySet()) {
//	                 if (e.getValue() != null && !e.getValue().toString().isBlank()) {
//	                     prompt.append("    ").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
//	                 }
//	             }
//	         }
//	         if (recordCount > 5) {
//	             prompt.append("... and ").append(recordCount - 5).append(" more records.\n");
//	         }
//	     }
//
//	     prompt.append("""
//	         
//	         OUTPUT INSTRUCTION:
//	         Start the response directly with HTML (<p>, <div>, or <table>).
//	         Do NOT include text like “Here is” or “Below is.”
//	         Only produce the HTML structure described.
//	         """);
//
//	     String aiResponse = ollamaChatModel.call(prompt.toString());
//
//	     aiResponse = aiResponse
//	         .replaceAll("(?s)```html|```", "")
//	         .replaceAll("^html\\s*", "")
//	         .trim();
//
//	     int htmlStart = aiResponse.indexOf("<");
//	     if (htmlStart > 0) {
//	         aiResponse = aiResponse.substring(htmlStart);
//	     }
//
//	     if (aiResponse.contains("|") && aiResponse.contains("---")) {
//	         aiResponse = convertMarkdownTableToHtml(aiResponse);
//	     }
//
//	     aiResponse = aiResponse.replaceAll("</start_of_turn>.*$", "").trim();
//
//	     Map<String, Object> response = new LinkedHashMap<>();
//	     response.put(" ", aiResponse.isEmpty()
//	         ? "<p>No relevant information was found.</p>"
//	         : aiResponse);
//
//	     return List.of(response);
//	 }


	 private String convertMarkdownTableToHtml(String markdown) {
	     try {
	         String[] lines = markdown.split("\n");
	         StringBuilder html = new StringBuilder();
	         boolean inTable = false;
	         boolean headerDone = false;
	         
	         html.append("<table style='width:100%; border-collapse:collapse; margin:15px 0; font-size:14px;'>\n");
	         
	         for (String line : lines) {
	             line = line.trim();
	             if (line.contains("|") && !line.contains("---")) {
	                 String[] cells = line.split("\\|");
	                 
	                 if (!headerDone && !inTable) {
	                     html.append("<thead style='background:linear-gradient(135deg,#667eea 0%,#764ba2 100%); color:white;'>\n<tr>\n");
	                     for (String cell : cells) {
	                         cell = cell.trim();
	                         if (!cell.isEmpty()) {
	                             html.append("<th style='padding:12px; text-align:left;'>").append(cell).append("</th>\n");
	                         }
	                     }
	                     html.append("</tr>\n</thead>\n<tbody>\n");
	                     inTable = true;
	                     headerDone = true;
	                 } else if (inTable && !line.contains("---")) {
	                     // Data row
	                     html.append("<tr style='border-bottom:1px solid #e0e0e0;'>\n");
	                     for (String cell : cells) {
	                         cell = cell.trim();
	                         if (!cell.isEmpty()) {
	                             html.append("<td style='padding:10px;'>").append(cell).append("</td>\n");
	                         }
	                     }
	                     html.append("</tr>\n");
	                 }
	             }
	         }
	         
	         html.append("</tbody>\n</table>");
	         return html.toString();
	         
	     } catch (Exception e) {
	         return markdown;
	     }
	 }
	 
	 
	 @Override
	 public List<Map<String, Object>> generateAnswerUsingOllamaWithPython(String que, List<Map<String, Object>> dbLevelAnswer) {
		 ChatIn input = new ChatIn(que, dbLevelAnswer);
	        ChatOut output = chatBotOllamaClient.generateAnswerUsingOllamaWithPython(input);
	        

		    String aiResponse = output.getAnswer()
		         .replaceAll("(?s)```html|```", "")
		         .replaceAll("^html\\s*", "")
		         .trim();

		     int htmlStart = aiResponse.indexOf("<");
		     if (htmlStart > 0) {
		         aiResponse = aiResponse.substring(htmlStart);
		     }

		     if (aiResponse.contains("|") && aiResponse.contains("---")) {
		         aiResponse = convertMarkdownTableToHtml(aiResponse);
		     }

		     aiResponse = aiResponse.replaceAll("</start_of_turn>.*$", "").trim();

		     Map<String, Object> response = new LinkedHashMap<>();
		     response.put(" ", aiResponse.isEmpty()
		         ? "<p>No relevant information was found.</p>"
		         : aiResponse);

		     return List.of(response);
		     
	 }
	 
}
