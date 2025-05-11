package com.library.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Send a notification to a specific user
     *
     * @param userId The user ID to send the notification to
     * @param type The type of notification (e.g., "LOAN_APPROVED", "LOAN_REJECTED", "BOOK_DUE_SOON")
     * @param message The notification message
     * @param data Additional data related to the notification
     */
    public void sendUserNotification(Long userId, String type, String message, Object data) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", type);
        notification.put("message", message);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("data", data);

        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/notifications",
            notification
        );
    }

    /**
     * Send a notification to all users with a specific role
     *
     * @param role The role to target (e.g., "ROLE_ADMIN", "ROLE_LIBRARIAN")
     * @param type The type of notification
     * @param message The notification message
     * @param data Additional data related to the notification
     */
    public void sendRoleNotification(String role, String type, String message, Object data) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", type);
        notification.put("message", message);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("data", data);

        messagingTemplate.convertAndSend(
            "/topic/role." + role,
            notification
        );
    }

    /**
     * Send a system-wide notification to all connected users
     *
     * @param type The type of notification
     * @param message The notification message
     * @param data Additional data related to the notification
     */
    public void sendGlobalNotification(String type, String message, Object data) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", type);
        notification.put("message", message);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("data", data);

        messagingTemplate.convertAndSend(
            "/topic/global",
            notification
        );
    }

    /**
     * Send a notification to all admin users
     *
     * @param type The type of notification
     * @param message The notification message
     * @param data Additional data related to the notification
     */
    public void sendAdminNotification(String type, String message, Object data) {
        // Send to both admin and librarian roles
        sendRoleNotification("ROLE_ADMIN", type, message, data);
        sendRoleNotification("ROLE_LIBRARIAN", type, message, data);
    }
}
