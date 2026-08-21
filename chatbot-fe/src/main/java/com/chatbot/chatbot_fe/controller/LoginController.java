package com.chatbot.chatbot_fe.controller;

import com.chatbot.chatbot_fe.service.AuthProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private AuthProxyService authProxy;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error",  required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error  != null) model.addAttribute("errorMsg",  "Invalid username or password.");
        if (logout != null) model.addAttribute("logoutMsg", "You have been logged out.");

        return "login"; // → templates/login.html
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam String password,
            Model model) {
        try {
            authProxy.register(email, username, password);
            model.addAttribute("successMsg", "Registration successful. You can now log in.");
        } catch (Exception ex) {
            model.addAttribute("errorMsg", "Registration failed. Check the fields or use a different username/email.");
        }
        return "register";
    }
}