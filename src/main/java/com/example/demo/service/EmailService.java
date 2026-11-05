package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(
            String toEmail,
            String token) {

        // Create reset link
        String resetLink =
                frontendUrl + "/reset-password?token=" + token;

        // Create email
        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(fromEmail);

        message.setTo(toEmail);

        message.setSubject("Password Reset Request");

        message.setText(
                "Hello,\n\n" +
                        "We received a request to reset your password.\n\n" +
                        "Click the link below to reset your password:\n\n" +
                        resetLink + "\n\n" +
                        "This link expires after 15 minutes.\n\n" +
                        "If you did not request this, ignore this email.\n\n" +
                        "Best Regards,\n" +
                        "Support Team"
        );

        // Send email
        mailSender.send(message);
    }
}