package com.dropdreamer.backend.controller;

import com.dropdreamer.backend.entity.User;
import com.dropdreamer.backend.repository.UserRepository;
import com.dropdreamer.backend.service.EmailService;
import com.dropdreamer.backend.util.JwtUtil;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class UserController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final EmailService emailService;

    // ✅ Correct constructor injection for all dependencies
    public UserController(UserRepository userRepository, EmailService emailService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
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

    // ✅ Login (with JWT)
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> requestBody) {
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

        if (!Boolean.TRUE.equals(user.isEmailVerified())) {
            return Map.of("message", "Email not verified");
        }

        if (!password.equals(user.getPassword())) {
            return Map.of("message", "Invalid password");
        }

        // ✅ Generate JWT Token
        String token = jwtUtil.generateToken(user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("token", token);
        response.put("email", user.getEmail());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());

        return response;
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    @GetMapping("/validate-token")
    public Map<String, Object> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Map<String, Object> response = new HashMap<>();

        try {
            String email = jwtUtil.extractUsername(token);
            boolean valid = !jwtUtil.isTokenExpired(token);
            response.put("valid", valid);
            response.put("email", email);
            response.put("message", valid ? "Token is valid" : "Token expired");
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "Invalid token");
        }

        return response;
    }
    @GetMapping("/test-token")
    public String testToken() {
        return "✅ Token is valid and user is authenticated!";
    }


}
