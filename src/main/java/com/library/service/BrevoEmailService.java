package com.library.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class BrevoEmailService {

    private static final Logger logger = LoggerFactory.getLogger(BrevoEmailService.class);
    private static final String BREVO_EMAIL_API_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${app.brevo.api-key}")
    private String apiKey;

    @Value("${app.brevo.sender-name}")
    private String senderName;

    @Value("${app.brevo.sender-email}")
    private String senderEmail;

    @Value("${app.email.debug-mode:false}")
    private boolean debugMode;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void sendEmail(String to, String subject, String body) {
        if (debugMode) {
            // Log the email instead of sending it
            logger.info("DEBUG MODE - Email would be sent via Brevo:");
            logger.info("To: {}", to);
            logger.info("Subject: {}", subject);
            logger.info("Body: {}", body);
            return;
        }

        try {
            // Set up headers with API key
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();

            // Set sender
            Map<String, String> sender = new HashMap<>();
            sender.put("name", senderName);
            sender.put("email", senderEmail);
            requestBody.put("sender", sender);

            // Set recipient
            Map<String, Object>[] recipients = new Map[1];
            recipients[0] = new HashMap<>();
            recipients[0].put("email", to);
            requestBody.put("to", recipients);

            // Set email content
            requestBody.put("subject", subject);
            requestBody.put("htmlContent", formatHtmlContent(body));

            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Send request to Brevo API
            String response = restTemplate.postForObject(BREVO_EMAIL_API_URL, entity, String.class);
            logger.info("Email sent successfully to: {}, Response: {}", to, response);
        } catch (Exception e) {
            logger.error("Failed to send email to: {} - Error: {}", to, e.getMessage());
            // Don't throw the exception to allow the application to continue
        }
    }

    public void sendOtpEmail(String to, String otp, String purpose) {
        if (debugMode) {
            // Log the email instead of sending it
            logger.info("DEBUG MODE - OTP Email would be sent via Brevo:");
            logger.info("To: {}", to);
            logger.info("Purpose: {}", purpose);
            logger.info("OTP: {}", otp);
            return;
        }

        try {
            // Set up headers with API key
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();

            // Set sender
            Map<String, String> sender = new HashMap<>();
            sender.put("name", senderName);
            sender.put("email", senderEmail);
            requestBody.put("sender", sender);

            // Set recipient
            Map<String, Object>[] recipients = new Map[1];
            recipients[0] = new HashMap<>();
            recipients[0].put("email", to);
            requestBody.put("to", recipients);

            // Set email subject based on purpose
            String subject = "Your OTP for " + purpose;
            requestBody.put("subject", subject);

            // Create HTML content with OTP
            String htmlContent = "<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                    "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>" +
                    "<div style='text-align: center; padding-bottom: 10px; border-bottom: 1px solid #eee;'>" +
                    "<h2 style='color: #4a90e2;'>Online Library</h2>" +
                    "</div>" +
                    "<div style='padding: 20px 0;'>" +
                    "<p>Hello,</p>" +
                    "<p>Your One-Time Password (OTP) for " + purpose + " is:</p>" +
                    "<div style='font-size: 24px; font-weight: bold; text-align: center; padding: 10px; margin: 20px 0; background-color: #f5f5f5; border-radius: 5px;'>" + otp + "</div>" +
                    "<p>This OTP is valid for 10 minutes. Please do not share it with anyone.</p>" +
                    "<p>If you did not request this, please ignore this email or contact support.</p>" +
                    "</div>" +
                    "<div style='text-align: center; font-size: 12px; color: #777; border-top: 1px solid #eee; padding-top: 10px;'>" +
                    "<p>&copy; " + java.time.Year.now().getValue() + " Online Library. All rights reserved.</p>" +
                    "</div>" +
                    "</div>" +
                    "</body></html>";

            requestBody.put("htmlContent", htmlContent);

            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Send request to Brevo API
            String response = restTemplate.postForObject(BREVO_EMAIL_API_URL, entity, String.class);

            logger.info("OTP email sent successfully to: {} for {}, Response: {}", to, purpose, response);
        } catch (Exception e) {
            logger.error("Failed to send OTP email to: {} - Error: {}", to, e.getMessage(), e);
        }
    }

    private String formatHtmlContent(String plainText) {
        // Convert plain text to simple HTML
        return "<html><body>" +
               plainText.replace("\n", "<br/>") +
               "</body></html>";
    }

    /**
     * Send a welcome email to a newly registered user
     *
     * @param to The recipient's email address
     * @param username The user's username
     */
    public void sendWelcomeEmail(String to, String username) {
        String subject = "Welcome to Online Library!";

        // Use online image link for logo
        String logoUrl = "https://img.icons8.com/color/96/000000/book-shelf.png";

        String htmlContent = "<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>" +
                "<div style='text-align: center; padding-bottom: 10px; border-bottom: 1px solid #eee;'>" +
                "<img src='" + logoUrl + "' alt='Online Library Logo' style='width: 80px; height: auto;'>" +
                "<h2 style='color: #4a90e2;'>Welcome to Online Library</h2>" +
                "</div>" +
                "<div style='padding: 20px 0;'>" +
                "<p>Hello " + username + ",</p>" +
                "<p>Thank you for registering with Online Library! Your account has been successfully created.</p>" +
                "<p>With your new account, you can:</p>" +
                "<ul>" +
                "<li>Browse our extensive collection of books</li>" +
                "<li>Borrow books online</li>" +
                "<li>Track your reading history</li>" +
                "<li>Receive personalized recommendations</li>" +
                "<li>Subscribe to premium plans for additional benefits</li>" +
                "</ul>" +
                "<p>If you have any questions or need assistance, please don't hesitate to contact our support team.</p>" +
                "<p>Happy reading!</p>" +
                "<p>The Online Library Team</p>" +
                "</div>" +
                "<div style='text-align: center; font-size: 12px; color: #777; border-top: 1px solid #eee; padding-top: 10px;'>" +
                "<p>&copy; " + java.time.Year.now().getValue() + " Online Library. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";

        try {
            // Set up headers with API key
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();

            // Set sender
            Map<String, String> sender = new HashMap<>();
            sender.put("name", senderName);
            sender.put("email", senderEmail);
            requestBody.put("sender", sender);

            // Set recipient
            Map<String, Object>[] recipients = new Map[1];
            recipients[0] = new HashMap<>();
            recipients[0].put("email", to);
            requestBody.put("to", recipients);

            // Set email content
            requestBody.put("subject", subject);
            requestBody.put("htmlContent", htmlContent);

            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            if (debugMode) {
                // Log the email instead of sending it
                logger.info("DEBUG MODE - Welcome Email would be sent via Brevo:");
                logger.info("To: {}", to);
                logger.info("Subject: {}", subject);
                logger.info("Content: {}", htmlContent);
                return;
            }

            // Send request to Brevo API
            String response = restTemplate.postForObject(BREVO_EMAIL_API_URL, entity, String.class);
            logger.info("Welcome email sent successfully to: {}, Response: {}", to, response);
        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {} - Error: {}", to, e.getMessage());
        }
    }

    /**
     * Send an account status notification email
     *
     * @param to The recipient's email address
     * @param username The user's username
     * @param status The new account status (blocked, unblocked, deleted)
     * @param reason The reason for the status change (optional)
     */
    public void sendAccountStatusEmail(String to, String username, String status, String reason) {
        String subject = "Your Online Library Account Status";
        String statusAction = "";
        String statusMessage = "";
        String statusColor = "";

        switch (status.toLowerCase()) {
            case "blocked":
                statusAction = "blocked";
                statusMessage = "Your account has been temporarily blocked by an administrator.";
                statusColor = "#e74c3c"; // Red
                break;
            case "unblocked":
                statusAction = "unblocked";
                statusMessage = "Your account has been unblocked and is now active again.";
                statusColor = "#2ecc71"; // Green
                break;
            case "deleted":
                statusAction = "deleted";
                statusMessage = "Your account has been permanently deleted from our system.";
                statusColor = "#e74c3c"; // Red
                break;
            default:
                statusAction = "updated";
                statusMessage = "There has been a change to your account status.";
                statusColor = "#3498db"; // Blue
        }

        String reasonSection = "";
        if (reason != null && !reason.isEmpty()) {
            reasonSection = "<p><strong>Reason:</strong> " + reason + "</p>";
        }

        String htmlContent = "<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>" +
                "<div style='text-align: center; padding-bottom: 10px; border-bottom: 1px solid #eee;'>" +
                "<h2 style='color: " + statusColor + ";'>Account " + statusAction.substring(0, 1).toUpperCase() + statusAction.substring(1) + "</h2>" +
                "</div>" +
                "<div style='padding: 20px 0;'>" +
                "<p>Hello " + username + ",</p>" +
                "<p>" + statusMessage + "</p>" +
                reasonSection +
                "<p>If you believe this action was taken in error or have any questions, please contact our support team for assistance.</p>" +
                "<p>The Online Library Team</p>" +
                "</div>" +
                "<div style='text-align: center; font-size: 12px; color: #777; border-top: 1px solid #eee; padding-top: 10px;'>" +
                "<p>&copy; " + java.time.Year.now().getValue() + " Online Library. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";

        try {
            // Set up headers with API key
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();

            // Set sender
            Map<String, String> sender = new HashMap<>();
            sender.put("name", senderName);
            sender.put("email", senderEmail);
            requestBody.put("sender", sender);

            // Set recipient
            Map<String, Object>[] recipients = new Map[1];
            recipients[0] = new HashMap<>();
            recipients[0].put("email", to);
            requestBody.put("to", recipients);

            // Set email content
            requestBody.put("subject", subject);
            requestBody.put("htmlContent", htmlContent);

            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            if (debugMode) {
                // Log the email instead of sending it
                logger.info("DEBUG MODE - Account Status Email would be sent via Brevo:");
                logger.info("To: {}", to);
                logger.info("Subject: {}", subject);
                logger.info("Status: {}", status);
                logger.info("Content: {}", htmlContent);
                return;
            }

            // Send request to Brevo API
            String response = restTemplate.postForObject(BREVO_EMAIL_API_URL, entity, String.class);
            logger.info("Account status email sent successfully to: {}, Status: {}, Response: {}", to, status, response);
        } catch (Exception e) {
            logger.error("Failed to send account status email to: {} - Error: {}", to, e.getMessage());
        }
    }

    /**
     * Send a subscription activation email
     *
     * @param to The recipient's email address
     * @param username The user's username
     * @param planName The subscription plan name
     * @param endDate The subscription end date
     */
    public void sendSubscriptionActivationEmail(String to, String username, String planName, LocalDate endDate) {
        String subject = "Your " + planName + " Subscription is Active";

        // Use online image links
        String logoUrl = "https://img.icons8.com/color/96/000000/book-shelf.png";
        String crownUrl = "https://img.icons8.com/color/96/000000/crown.png";

        String formattedEndDate = endDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));

        String htmlContent = "<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>" +
                "<div style='text-align: center; padding-bottom: 10px; border-bottom: 1px solid #eee;'>" +
                "<img src='" + logoUrl + "' alt='Online Library Logo' style='width: 80px; height: auto;'>" +
                "<h2 style='color: #4a90e2;'>Subscription Activated</h2>" +
                "</div>" +
                "<div style='padding: 20px 0; text-align: center;'>" +
                "<img src='" + crownUrl + "' alt='Premium' style='width: 60px; height: auto;'>" +
                "<h3 style='color: #f39c12;'>" + planName + " Plan</h3>" +
                "<p>Hello " + username + ",</p>" +
                "<p>Your " + planName + " subscription has been successfully activated!</p>" +
                "<p>Your subscription will be valid until <strong>" + formattedEndDate + "</strong>.</p>" +
                "<div style='background-color: #f8f9fa; border-radius: 5px; padding: 15px; margin: 20px 0; text-align: left;'>" +
                "<h4 style='margin-top: 0;'>Your Benefits:</h4>" +
                "<ul>" +
                getPlanBenefits(planName) +
                "</ul>" +
                "</div>" +
                "<p>Thank you for subscribing to our premium service. Enjoy your enhanced library experience!</p>" +
                "<p>The Online Library Team</p>" +
                "</div>" +
                "<div style='text-align: center; font-size: 12px; color: #777; border-top: 1px solid #eee; padding-top: 10px;'>" +
                "<p>&copy; " + java.time.Year.now().getValue() + " Online Library. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";

        try {
            // Set up headers with API key
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();

            // Set sender
            Map<String, String> sender = new HashMap<>();
            sender.put("name", senderName);
            sender.put("email", senderEmail);
            requestBody.put("sender", sender);

            // Set recipient
            Map<String, Object>[] recipients = new Map[1];
            recipients[0] = new HashMap<>();
            recipients[0].put("email", to);
            requestBody.put("to", recipients);

            // Set email content
            requestBody.put("subject", subject);
            requestBody.put("htmlContent", htmlContent);

            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            if (debugMode) {
                // Log the email instead of sending it
                logger.info("DEBUG MODE - Subscription Activation Email would be sent via Brevo:");
                logger.info("To: {}", to);
                logger.info("Subject: {}", subject);
                logger.info("Content: {}", htmlContent);
                return;
            }

            // Send request to Brevo API
            String response = restTemplate.postForObject(BREVO_EMAIL_API_URL, entity, String.class);
            logger.info("Subscription activation email sent successfully to: {}, Plan: {}, Response: {}", to, planName, response);
        } catch (Exception e) {
            logger.error("Failed to send subscription activation email to: {} - Error: {}", to, e.getMessage());
        }
    }

    /**
     * Send a subscription cancellation email
     *
     * @param to The recipient's email address
     * @param username The user's username
     * @param planName The subscription plan name
     * @param endDate The subscription end date (when access will end)
     */
    public void sendSubscriptionCancellationEmail(String to, String username, String planName, LocalDate endDate) {
        String subject = "Your " + planName + " Subscription Has Been Cancelled";

        // Use online image links
        String logoUrl = "https://img.icons8.com/color/96/000000/book-shelf.png";

        String formattedEndDate = endDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));

        String htmlContent = "<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>" +
                "<div style='text-align: center; padding-bottom: 10px; border-bottom: 1px solid #eee;'>" +
                "<img src='" + logoUrl + "' alt='Online Library Logo' style='width: 80px; height: auto;'>" +
                "<h2 style='color: #e74c3c;'>Subscription Cancelled</h2>" +
                "</div>" +
                "<div style='padding: 20px 0;'>" +
                "<p>Hello " + username + ",</p>" +
                "<p>Your " + planName + " subscription has been cancelled as requested.</p>" +
                "<p>You will still have access to all premium features until <strong>" + formattedEndDate + "</strong>.</p>" +
                "<p>After this date, your account will revert to the standard plan.</p>" +
                "<p>If you change your mind, you can resubscribe at any time from your account dashboard.</p>" +
                "<p>Thank you for being a premium subscriber. We hope to see you again soon!</p>" +
                "<p>The Online Library Team</p>" +
                "</div>" +
                "<div style='text-align: center; font-size: 12px; color: #777; border-top: 1px solid #eee; padding-top: 10px;'>" +
                "<p>&copy; " + java.time.Year.now().getValue() + " Online Library. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";

        try {
            // Set up headers with API key
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();

            // Set sender
            Map<String, String> sender = new HashMap<>();
            sender.put("name", senderName);
            sender.put("email", senderEmail);
            requestBody.put("sender", sender);

            // Set recipient
            Map<String, Object>[] recipients = new Map[1];
            recipients[0] = new HashMap<>();
            recipients[0].put("email", to);
            requestBody.put("to", recipients);

            // Set email content
            requestBody.put("subject", subject);
            requestBody.put("htmlContent", htmlContent);

            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            if (debugMode) {
                // Log the email instead of sending it
                logger.info("DEBUG MODE - Subscription Cancellation Email would be sent via Brevo:");
                logger.info("To: {}", to);
                logger.info("Subject: {}", subject);
                logger.info("Content: {}", htmlContent);
                return;
            }

            // Send request to Brevo API
            String response = restTemplate.postForObject(BREVO_EMAIL_API_URL, entity, String.class);
            logger.info("Subscription cancellation email sent successfully to: {}, Plan: {}, Response: {}", to, planName, response);
        } catch (Exception e) {
            logger.error("Failed to send subscription cancellation email to: {} - Error: {}", to, e.getMessage());
        }
    }

    /**
     * Send a subscription change email (upgrade or downgrade)
     *
     * @param to The recipient's email address
     * @param username The user's username
     * @param oldPlanName The old subscription plan name
     * @param newPlanName The new subscription plan name
     * @param isUpgrade Whether this is an upgrade or downgrade
     * @param endDate The new subscription end date
     */
    public void sendSubscriptionChangeEmail(String to, String username, String oldPlanName, String newPlanName, boolean isUpgrade, LocalDate endDate) {
        String action = isUpgrade ? "Upgraded" : "Downgraded";
        String subject = "Your Subscription Has Been " + action;

        // Use online image links
        String logoUrl = "https://img.icons8.com/color/96/000000/book-shelf.png";
        String iconUrl = isUpgrade
            ? "https://img.icons8.com/color/96/000000/up--v1.png"  // Up arrow for upgrade
            : "https://img.icons8.com/color/96/000000/down--v1.png"; // Down arrow for downgrade

        String formattedEndDate = endDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        String color = isUpgrade ? "#2ecc71" : "#f39c12"; // Green for upgrade, orange for downgrade

        String htmlContent = "<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>" +
                "<div style='text-align: center; padding-bottom: 10px; border-bottom: 1px solid #eee;'>" +
                "<img src='" + logoUrl + "' alt='Online Library Logo' style='width: 80px; height: auto;'>" +
                "<h2 style='color: " + color + ";'>Subscription " + action + "</h2>" +
                "</div>" +
                "<div style='padding: 20px 0; text-align: center;'>" +
                "<img src='" + iconUrl + "' alt='" + action + "' style='width: 60px; height: auto;'>" +
                "<p>Hello " + username + ",</p>" +
                "<p>Your subscription has been " + action.toLowerCase() + " from <strong>" + oldPlanName + "</strong> to <strong>" + newPlanName + "</strong>.</p>" +
                "<p>Your new subscription will be valid until <strong>" + formattedEndDate + "</strong>.</p>" +
                "<div style='background-color: #f8f9fa; border-radius: 5px; padding: 15px; margin: 20px 0; text-align: left;'>" +
                "<h4 style='margin-top: 0;'>Your New Benefits:</h4>" +
                "<ul>" +
                getPlanBenefits(newPlanName) +
                "</ul>" +
                "</div>" +
                "<p>Thank you for your continued support. Enjoy your " + newPlanName + " subscription!</p>" +
                "<p>The Online Library Team</p>" +
                "</div>" +
                "<div style='text-align: center; font-size: 12px; color: #777; border-top: 1px solid #eee; padding-top: 10px;'>" +
                "<p>&copy; " + java.time.Year.now().getValue() + " Online Library. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";

        try {
            // Set up headers with API key
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();

            // Set sender
            Map<String, String> sender = new HashMap<>();
            sender.put("name", senderName);
            sender.put("email", senderEmail);
            requestBody.put("sender", sender);

            // Set recipient
            Map<String, Object>[] recipients = new Map[1];
            recipients[0] = new HashMap<>();
            recipients[0].put("email", to);
            requestBody.put("to", recipients);

            // Set email content
            requestBody.put("subject", subject);
            requestBody.put("htmlContent", htmlContent);

            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            if (debugMode) {
                // Log the email instead of sending it
                logger.info("DEBUG MODE - Subscription Change Email would be sent via Brevo:");
                logger.info("To: {}", to);
                logger.info("Subject: {}", subject);
                logger.info("Content: {}", htmlContent);
                return;
            }

            // Send request to Brevo API
            String response = restTemplate.postForObject(BREVO_EMAIL_API_URL, entity, String.class);
            logger.info("Subscription change email sent successfully to: {}, From: {}, To: {}, Response: {}",
                    to, oldPlanName, newPlanName, response);
        } catch (Exception e) {
            logger.error("Failed to send subscription change email to: {} - Error: {}", to, e.getMessage());
        }
    }

    /**
     * Helper method to get plan benefits based on plan name
     */
    private String getPlanBenefits(String planName) {
        switch (planName.toUpperCase()) {
            case "BASIC":
                return "<li>Borrow up to 3 books at a time</li>" +
                       "<li>14-day loan period</li>" +
                       "<li>3-day grace period for returns</li>" +
                       "<li>Standard reservation priority</li>";
            case "STANDARD":
                return "<li>Borrow up to 5 books at a time</li>" +
                       "<li>21-day loan period</li>" +
                       "<li>5-day grace period for returns</li>" +
                       "<li>Higher reservation priority</li>" +
                       "<li>10% discount on late fees</li>";
            case "PREMIUM":
                return "<li>Borrow up to 8 books at a time</li>" +
                       "<li>30-day loan period</li>" +
                       "<li>7-day grace period for returns</li>" +
                       "<li>Highest reservation priority</li>" +
                       "<li>25% discount on late fees</li>" +
                       "<li>Access to exclusive premium content</li>" +
                       "<li>Priority customer support</li>";
            default:
                return "<li>Standard library benefits</li>";
        }
    }
}
