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
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Drop Dreamer - OTP Verification");
        message.setText("Your OTP for email verification is: " + otp + "\n\nThank you for registering with Drop Dreamer!");
        mailSender.send(message);
    }
}
