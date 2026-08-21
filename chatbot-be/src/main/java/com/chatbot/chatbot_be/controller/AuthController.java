package com.chatbot.chatbot_be.controller;

import com.chatbot.chatbot_be.service.HrmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:9091")
public class AuthController {

    @Autowired
    private HrmsService service;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        Map<String, Object> user = service.authenticateUser(
                request.get("username"), request.get("password"));
        return user.isEmpty()
                ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid username or password"))
                : ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        int result = service.registerUser(
                request.get("email"), request.get("username"), request.get("password"));
        if (result == 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Registration failed. Check the fields or use a different username/email."));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Registration successful");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}