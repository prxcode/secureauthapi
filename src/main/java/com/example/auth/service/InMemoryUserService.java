package com.example.auth.service;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.auth.model.User;

@Service
public class InMemoryUserService {

    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    public void saveUser(User user) {
        users.put(user.getUsername(), user);
    }

    public User findByUsername(String username) {
        return users.get(username);
    }
}