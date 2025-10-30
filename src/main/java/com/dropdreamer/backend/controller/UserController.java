package com.dropdreamer.backend.controller;

import com.dropdreamer.backend.entity.User;
import com.dropdreamer.backend.repository.UserRepository;
import com.dropdreamer.backend.service.EmailService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserController(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // ✅ Signup (register new user)
    @PostMapping("/signup")
    public Map<String, String> signup(@RequestBody Map<String, String> requestBody) {
        String firstName = requestBody.get("firstName");
        String lastName = requestBody.get("lastName");
        String email = requestBody.get("email");
        String mobile = requestBody.get("mobile");
        String password = requestBody.get("password");

        if (firstName == null || lastName == null || email == null || password == null) {
            return Map.of("message", "Missing required fields");
        }

        if (userRepository.existsByEmail(email)) {
            return Map.of("message", "User already exists");
        }

        // Generate 6-digit OTP
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        User user = new User(firstName, lastName, email, mobile, password);
        user.setOtp(otp);
        user.setEmailVerified(false);
        userRepository.save(user);

        // Send OTP via Email
        emailService.sendOtpEmail(email, otp);

        return Map.of(
                "message", "Signup successful. OTP sent to email.",
                "email", email
        );
    }

    // ✅ Verify OTP
    @PostMapping("/verify-otp")
    public Map<String, String> verifyOtp(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String otp = requestBody.get("otp");

        if (email == null || otp == null) {
            return Map.of("message", "Missing email or OTP");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return Map.of("message", "User not found");
        }

        User user = userOpt.get();

        if (otp.equals(user.getOtp())) {
            user.setEmailVerified(true);
            user.setOtp(null);
            userRepository.save(user);
            return Map.of("message", "Email verified successfully");
        } else {
            return Map.of("message", "Invalid OTP");
        }
    }
    // ✅ Login
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String password = requestBody.get("password");

        if (email == null || password == null) {
            return Map.of("message", "Email and password are required");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return Map.of("message", "User not found");
        }

        User user = userOpt.get();

        // Check if email is verified first
        if (!Boolean.TRUE.equals(user.isEmailVerified())) {
            return Map.of("message", "Email not verified");
        }

        // Match password
        if (!password.equals(user.getPassword())) {
            return Map.of("message", "Invalid password");
        }

        return Map.of(
                "message", "Login successful",
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName()
        );
    }
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // other endpoints remain the same...
}
