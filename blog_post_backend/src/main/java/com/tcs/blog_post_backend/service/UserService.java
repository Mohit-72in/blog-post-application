package com.tcs.blog_post_backend.service;

import com.tcs.blog_post_backend.dto.LoginRequest;
import com.tcs.blog_post_backend.dto.SignupRequest;
import com.tcs.blog_post_backend.exception.ResourceNotFoundException;
import com.tcs.blog_post_backend.model.User;
import com.tcs.blog_post_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User signup(SignupRequest request) {
        String cleanUsername = request.getUsername().trim().toLowerCase();
        if (userRepository.existsByUsername(cleanUsername)) {
            throw new IllegalArgumentException("Username already taken. Please choose another username.");
        }
        User user = new User(cleanUsername, request.getName().trim());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User login(LoginRequest request) {
        String cleanUsername = request.getUsername().trim().toLowerCase();
        return userRepository.findByUsername(cleanUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User '@" + cleanUsername + "' not found. Please sign up first."));
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        String cleanUsername = username.trim().toLowerCase();
        return userRepository.findByUsername(cleanUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
