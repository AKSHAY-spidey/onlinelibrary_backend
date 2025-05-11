package com.library.controller;

import com.library.model.User;
import com.library.repository.UserRepository;
import com.library.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Process a message sent to the chatbot
     *
     * @param request The message request containing the user's message
     * @return A response with the chatbot's reply
     */
    @PostMapping("/message")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> processMessage(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
        }
        
        // Get the current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        
        User user = userOptional.get();
        
        // Process the message (in a real implementation, this would call an AI service)
        String response = processMessageLocally(message, user);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("text", response);
        responseData.put("type", "text");
        
        return ResponseEntity.ok(responseData);
    }
    
    /**
     * Get suggestions for the chatbot based on user context
     *
     * @param request The context request
     * @return A list of suggestions
     */
    @PostMapping("/suggestions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getSuggestions(@RequestBody Map<String, String> request) {
        String context = request.get("context");
        
        // Get the current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        
        User user = userOptional.get();
        
        // Generate suggestions based on user context and profile
        // In a real implementation, this would be more sophisticated
        String[] suggestions = {
            "How do I borrow a book?",
            "What subscription plans do you offer?",
            "How do I pay fines?",
            "How to return a book?",
            "Contact support"
        };
        
        return ResponseEntity.ok(Map.of("suggestions", suggestions));
    }
    
    /**
     * Process a message locally without calling an external AI service
     * This is a fallback method for demonstration purposes
     *
     * @param message The user's message
     * @param user The current user
     * @return A response message
     */
    private String processMessageLocally(String message, User user) {
        message = message.toLowerCase().trim();
        
        // Library-specific responses
        if (message.contains("borrow") || message.contains("loan") || message.contains("check out")) {
            return "To borrow a book, browse our collection, select a book, and click the 'Borrow' button. You'll need to be logged in and have an active subscription. Your loan will be processed by our librarians.";
        }
        
        if (message.contains("return") || message.contains("give back")) {
            return "To return a book, go to 'My Loans' in your account, find the book you want to return, and click 'Return Book'. A librarian will process your return.";
        }
        
        if (message.contains("fine") || message.contains("penalty") || message.contains("late fee")) {
            return "Fines are charged for overdue books. The rate depends on your subscription plan. You can pay fines through the 'My Loans' section in your account.";
        }
        
        if (message.contains("subscription") || message.contains("plan") || message.contains("membership")) {
            return "We offer three subscription plans: Basic, Standard, and Premium. Each plan offers different benefits like maximum books allowed and loan duration. You can subscribe or change your plan in the 'Subscription' section.";
        }
        
        if (message.contains("payment") || message.contains("pay") || message.contains("billing")) {
            return "We accept payments through Razorpay. You can pay for subscriptions, fines, and other services securely through our integrated payment gateway.";
        }
        
        if (message.contains("account") || message.contains("profile") || message.contains("settings")) {
            return "You can manage your account details, update your profile, change password, and adjust notification preferences in the 'Profile' section.";
        }
        
        if (message.contains("contact") || message.contains("help") || message.contains("support")) {
            return "For assistance, please contact our support team at support@onlinelibrary.com or visit the 'Contact Us' page.";
        }
        
        if (message.contains("hours") || message.contains("timing") || message.contains("open")) {
            return "Our online library is available 24/7. For physical pickups or returns, our locations are open Monday to Friday, 9 AM to 6 PM, and Saturday, 10 AM to 4 PM.";
        }
        
        if (message.contains("recommend") || message.contains("suggestion") || message.contains("what should i read")) {
            return "Based on your reading history, you might enjoy exploring our 'For You' section which has personalized recommendations. You can also check our 'Popular' and 'New Arrivals' sections.";
        }
        
        if (message.contains("wishlist") || message.contains("save for later") || message.contains("bookmark")) {
            return "You can add books to your wishlist by clicking the heart icon on any book. View and manage your wishlist from the 'Wishlist' section in your account.";
        }
        
        if (message.contains("hello") || message.contains("hi") || message.contains("hey")) {
            return "Hello " + user.getUsername() + "! I'm your Online Library assistant. How can I help you today?";
        }
        
        if (message.contains("thank")) {
            return "You're welcome! Is there anything else I can help you with?";
        }
        
        if (message.contains("bye") || message.contains("goodbye")) {
            return "Goodbye! Feel free to chat again if you have more questions.";
        }
        
        // Default response
        return "I'm not sure how to help with that specific query. You can ask me about borrowing books, returns, fines, subscriptions, payments, or your account. For more complex issues, please contact our support team.";
    }
}
