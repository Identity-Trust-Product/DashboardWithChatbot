package com.chatbot.chatbot_fe.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AuthProxyService {

    @Value("${be.base.url:http://localhost:9090}")
    private String beBaseUrl;

    private final RestTemplate rest = new RestTemplate();

    public Map<String, Object> login(String username, String password) {
        return post("/api/auth/login", Map.of("username", username, "password", password));
    }

    public Map<String, Object> register(String email, String username, String password) {
        return post("/api/auth/register", Map.of(
                "email", email, "username", username, "password", password));
    }

    private Map<String, Object> post(String path, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map<String, Object>> response = rest.exchange(
                beBaseUrl + path, HttpMethod.POST, new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<Map<String, Object>>() {});
        return response.getBody() == null ? Map.of() : response.getBody();
    }
}