package com.library.service;

import java.time.LocalDate;

public interface EmailService {
    /**
     * Send OTP verification email
     *
     * @param to Recipient email
     * @param username Username
     * @param otp One-time password
     * @param type Type of OTP (registration, password reset, etc.)
     */
    void sendOtpEmail(String to, String username, String otp, String type);

    /**
     * Send account status email (activated, blocked, deleted, etc.)
     *
     * @param to Recipient email
     * @param username Username
     * @param status Account status
     * @param message Additional message
     */
    void sendAccountStatusEmail(String to, String username, String status, String message);

    /**
     * Send loan status email (approved, rejected, overdue, etc.)
     *
     * @param to Recipient email
     * @param username Username
     * @param bookTitle Book title
     * @param status Loan status
     * @param dueDate Due date (if applicable)
     */
    void sendLoanStatusEmail(String to, String username, String bookTitle, String status, LocalDate dueDate);

    /**
     * Send wishlist availability email
     *
     * @param to Recipient email
     * @param username Username
     * @param bookTitle Book title
     * @param author Book author
     */
    void sendWishlistAvailabilityEmail(String to, String username, String bookTitle, String author);

    /**
     * Send auto-borrow email
     *
     * @param to Recipient email
     * @param username Username
     * @param bookTitle Book title
     * @param author Book author
     * @param dueDate Due date
     */
    void sendAutoBorrowEmail(String to, String username, String bookTitle, String author, LocalDate dueDate);

    /**
     * Send reservation status email
     *
     * @param to Recipient email
     * @param username Username
     * @param bookTitle Book title
     * @param status Reservation status
     * @param expiryDate Expiry date (if applicable)
     */
    void sendReservationStatusEmail(String to, String username, String bookTitle, String status, LocalDate expiryDate);

    /**
     * Send payment receipt email
     *
     * @param to Recipient email
     * @param username Username
     * @param amount Payment amount
     * @param paymentId Payment ID
     * @param description Payment description
     */
    void sendPaymentReceiptEmail(String to, String username, double amount, String paymentId, String description);

    /**
     * Send payment email for various payment events
     *
     * @param to Recipient email
     * @param username Username
     * @param template Email template name
     * @param subject Email subject
     * @param itemName Name of the item being paid for (book title, subscription plan, etc.)
     * @param amount Payment amount
     * @param currency Currency code (e.g., INR)
     * @param receiptNumber Receipt number
     */
    void sendPaymentEmail(String to, String username, String template, String subject, String itemName, Double amount, String currency, String receiptNumber);
}
