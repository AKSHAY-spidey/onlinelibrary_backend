package com.library.service.impl;

import com.library.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${spring.mail.username:noreply@example.com}")
    private String fromEmail;

    @Value("${app.name:Online Library}")
    private String appName;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void sendOtpEmail(String to, String username, String otp, String type) {
        // Log the email that would be sent
        System.out.println("Sending OTP email to: " + to);
        System.out.println("Subject: " + appName + " - " + (type.equals("registration") ? "Email Verification" : "Password Reset"));
        System.out.println("OTP: " + otp);
    }

    @Override
    public void sendAccountStatusEmail(String to, String username, String status, String message) {
        // Log the email that would be sent
        System.out.println("Sending account status email to: " + to);
        System.out.println("Subject: " + appName + " - Account " + capitalize(status));
        System.out.println("Status: " + status);
        System.out.println("Message: " + message);
    }

    @Override
    public void sendLoanStatusEmail(String to, String username, String bookTitle, String status, LocalDate dueDate) {
        // Log the email that would be sent
        System.out.println("Sending loan status email to: " + to);
        System.out.println("Subject: " + appName + " - Loan " + capitalize(status));
        System.out.println("Book: " + bookTitle);
        System.out.println("Status: " + status);
        if (dueDate != null) {
            System.out.println("Due Date: " + dueDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        }
    }

    @Override
    public void sendWishlistAvailabilityEmail(String to, String username, String bookTitle, String author) {
        // Log the email that would be sent
        System.out.println("Sending wishlist availability email to: " + to);
        System.out.println("Subject: " + appName + " - Book Available: " + bookTitle);
        System.out.println("Book: " + bookTitle);
        System.out.println("Author: " + author);
    }

    @Override
    public void sendAutoBorrowEmail(String to, String username, String bookTitle, String author, LocalDate dueDate) {
        // Log the email that would be sent
        System.out.println("Sending auto-borrow email to: " + to);
        System.out.println("Subject: " + appName + " - Book Automatically Borrowed: " + bookTitle);
        System.out.println("Book: " + bookTitle);
        System.out.println("Author: " + author);
        System.out.println("Due Date: " + dueDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
    }

    @Override
    public void sendReservationStatusEmail(String to, String username, String bookTitle, String status, LocalDate expiryDate) {
        // Log the email that would be sent
        System.out.println("Sending reservation status email to: " + to);
        System.out.println("Subject: " + appName + " - Reservation " + capitalize(status));
        System.out.println("Book: " + bookTitle);
        System.out.println("Status: " + status);
        if (expiryDate != null) {
            System.out.println("Expiry Date: " + expiryDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        }
    }

    @Override
    public void sendPaymentReceiptEmail(String to, String username, double amount, String paymentId, String description) {
        // Log the email that would be sent
        System.out.println("Sending payment receipt email to: " + to);
        System.out.println("Subject: " + appName + " - Payment Receipt");
        System.out.println("Amount: " + String.format("%.2f", amount));
        System.out.println("Payment ID: " + paymentId);
        System.out.println("Description: " + description);
        System.out.println("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
    }

    @Override
    public void sendPaymentEmail(String to, String username, String template, String subject, String itemName, Double amount, String currency, String receiptNumber) {
        // Log the email that would be sent
        System.out.println("Sending payment email to: " + to);
        System.out.println("Subject: " + appName + " - " + subject);
        System.out.println("Template: " + template);
        System.out.println("Item: " + itemName);
        System.out.println("Amount: " + String.format("%.2f", amount) + " " + currency);
        System.out.println("Receipt Number: " + receiptNumber);
        System.out.println("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

        // In a real implementation, this would send an email using the template
        // For now, we just log the details
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
