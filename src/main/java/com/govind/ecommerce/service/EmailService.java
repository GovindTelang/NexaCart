package com.govind.ecommerce.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${MAIL_FROM:${spring.mail.username}}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmation(String recipient) throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(senderEmail, "NexaCart");
        helper.setTo(recipient);
        helper.setSubject("NexaCart — Order Confirmed");

        String html = """
                <div style="font-family:Arial,sans-serif;max-width:600px;
                            margin:auto;padding:32px;color:#222;">
                    
                    <div style="border-bottom:1px solid #eee;padding-bottom:18px;">
                        <h1 style="margin:0;color:#ff6600;">NexaCart</h1>
                    </div>

                    <div style="padding:28px 0;">
                        <h2 style="margin-top:0;">Your order is confirmed 🎉</h2>

                        <p style="font-size:16px;line-height:1.7;">
                            Thank you for shopping with NexaCart.
                            Your payment was successfully verified and
                            your order has been placed.
                        </p>

                        <div style="background:#f8f6f3;padding:18px;
                                    border-radius:12px;margin:24px 0;">
                            <strong>Payment status</strong>
                            <div style="margin-top:8px;color:#26734d;">
                                ✓ Paid and verified
                            </div>
                        </div>

                        <p style="line-height:1.7;">
                            You can view your order and its current status
                            from the <strong>My Orders</strong> section
                            of NexaCart.
                        </p>

                        <p style="margin-top:30px;color:#777;">
                            Thanks for choosing NexaCart.<br>
                            Everything you need. Nothing you don't.
                        </p>
                    </div>

                    <div style="border-top:1px solid #eee;padding-top:18px;
                                color:#888;font-size:12px;">
                        © 2026 NexaCart · Built with Spring Boot + React
                    </div>
                </div>
                """;

        helper.setText(html, true);
        mailSender.send(message);
    }
}