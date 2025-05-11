package com.library.controller;

import com.library.service.BrevoEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private BrevoEmailService emailService;

    @GetMapping("/email")
    public ResponseEntity<?> testEmail(@RequestParam String to) {
        emailService.sendEmail(to, "Test Email from Online Library", 
                "This is a test email from the Online Library Management System.\n\n" +
                "If you received this email, it means the email service is working correctly.");
        
        return ResponseEntity.ok("Test email sent to: " + to);
    }
}
