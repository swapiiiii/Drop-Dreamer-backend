package com.dropdreamer.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("9a93f1001@smtp-brevo.com"); // Brevo verified sender
            message.setTo(toEmail);
            message.setSubject("Drop Dreamer - OTP Verification");
            message.setText("Your OTP for email verification is: " + otp + "\n\nThank you for registering with Drop Dreamer!");
            mailSender.send(message);
            System.out.println("✅ OTP email sent successfully to " + toEmail);
        } catch (Exception e) {
            System.out.println("❌ Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
