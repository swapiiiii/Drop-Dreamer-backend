package com.dropdreamer.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 🔹 Build the request body
            Map<String, Object> emailBody = new HashMap<>();
            emailBody.put("sender", Map.of("name", "Drop Dreamer", "email", "noreply@dropdreamer.com"));
            emailBody.put("to", List.of(Map.of("email", toEmail)));
            emailBody.put("subject", "Drop Dreamer - OTP Verification");
            emailBody.put(
                    "htmlContent",
                    "<p>Hi,</p>" +
                            "<p>Your OTP for email verification is: <b>" + otp + "</b></p>" +
                            "<p>It will expire in 10 minutes.</p>" +
                            "<br><p>Thank you for registering with <b>Drop Dreamer</b>!</p>"
            );

            // 🔹 Headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("accept", "application/json");
            headers.set("api-key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(emailBody, headers);

            // 🔹 Send the email via Brevo REST API
            ResponseEntity<String> response = restTemplate.exchange(BREVO_URL, HttpMethod.POST, entity, String.class);

            System.out.println("📧 Email sent successfully: " + response.getStatusCode());
            System.out.println("📨 Brevo response: " + response.getBody());

        } catch (Exception e) {
            System.err.println("❌ Error sending email via Brevo: " + e.getMessage());
        }
    }
}
