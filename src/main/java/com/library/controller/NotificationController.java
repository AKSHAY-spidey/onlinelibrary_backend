package com.library.controller;

import com.library.payload.response.MessageResponse;
import com.library.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * WebSocket endpoint for user connection
     */
    @MessageMapping("/connect")
    public void connect(SimpMessageHeaderAccessor headerAccessor) {
        // This method is called when a user connects to the WebSocket
        // We can use it to track connected users if needed
    }

    /**
     * REST endpoint for sending a notification to a specific user
     * Only accessible by admins and librarians
     */
    @PostMapping("/user")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<MessageResponse> sendUserNotification(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        String type = (String) payload.get("type");
        String message = (String) payload.get("message");
        Object data = payload.get("data");

        notificationService.sendUserNotification(userId, type, message, data);
        return ResponseEntity.ok(new MessageResponse("Notification sent successfully"));
    }

    /**
     * REST endpoint for sending a notification to all users with a specific role
     * Only accessible by admins
     */
    @PostMapping("/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> sendRoleNotification(@RequestBody Map<String, Object> payload) {
        String role = (String) payload.get("role");
        String type = (String) payload.get("type");
        String message = (String) payload.get("message");
        Object data = payload.get("data");

        notificationService.sendRoleNotification(role, type, message, data);
        return ResponseEntity.ok(new MessageResponse("Notification sent to role: " + role));
    }

    /**
     * REST endpoint for sending a global notification to all users
     * Only accessible by admins
     */
    @PostMapping("/global")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> sendGlobalNotification(@RequestBody Map<String, Object> payload) {
        String type = (String) payload.get("type");
        String message = (String) payload.get("message");
        Object data = payload.get("data");

        notificationService.sendGlobalNotification(type, message, data);
        return ResponseEntity.ok(new MessageResponse("Global notification sent"));
    }
}
