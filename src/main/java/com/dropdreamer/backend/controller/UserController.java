package com.dropdreamer.backend.controller;

import com.dropdreamer.backend.entity.User;
import com.dropdreamer.backend.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // POST endpoint to add a user
    @PostMapping
    public User addUser(@RequestBody Map<String, String> requestBody) {
        String userName = requestBody.get("username"); // match Angular field name
        User user = new User(userName);
        return userRepository.save(user);
    }

    // GET endpoint to fetch all users
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Optional: GET by ID
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
