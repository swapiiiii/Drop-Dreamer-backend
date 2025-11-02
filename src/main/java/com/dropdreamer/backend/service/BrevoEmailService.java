package com.dropdreamer.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class BrevoEmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    public void sendOtpEmail(String toEmail, String otp) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> body = new HashMap<>();
        Map<String, String> sender = Map.of("name", "Drop Dreamer", "email", "no-reply@dropdreamer.com");
        Map<String, String> to = Map.of("email", toEmail);

        body.put("sender", sender);
        body.put("to", List.of(to));
        body.put("subject", "Drop Dreamer - OTP Verification");
        body.put("htmlContent",
                "<p>Your OTP for email verification is: <b>" + otp + "</b></p>" +
                        "<p>Thank you for registering with Drop Dreamer!</p>");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);
        System.out.println("🔑 Brevo API Key (first 10 chars): " + apiKey.substring(0, 10));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_API_URL, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Email sent successfully via Brevo API");
            } else {
                System.err.println("❌ Failed to send email via Brevo API: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Error sending email via Brevo API: " + e.getMessage());
        }
    }
}
