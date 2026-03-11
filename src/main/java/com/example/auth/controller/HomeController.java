package com.example.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class HomeController {

    // Serve home page after login
    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "Guest";
        model.addAttribute("username", username);
        return "home"; // maps to src/main/resources/templates/home.html
    }

    // Handle logout and redirect to home page
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return "redirect:/"; // redirect back to home page
    }
}