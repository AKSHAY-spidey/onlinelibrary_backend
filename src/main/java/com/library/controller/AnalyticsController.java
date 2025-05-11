package com.library.controller;

import com.library.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * Get comprehensive library usage statistics
     * Only accessible by admins
     *
     * @return A map containing various library usage statistics
     */
    @GetMapping("/library")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getLibraryStats() {
        Map<String, Object> stats = analyticsService.getLibraryStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get user activity analytics
     * Accessible by the user themselves, librarians, and admins
     *
     * @param userId The ID of the user to get analytics for
     * @return A map containing user activity statistics
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or (hasRole('USER') and #userId == authentication.principal.id)")
    public ResponseEntity<Map<String, Object>> getUserAnalytics(@PathVariable Long userId) {
        Map<String, Object> analytics = analyticsService.getUserAnalytics(userId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get book analytics
     * Accessible by librarians and admins
     *
     * @param bookId The ID of the book to get analytics for
     * @return A map containing book analytics
     */
    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<Map<String, Object>> getBookAnalytics(@PathVariable Long bookId) {
        Map<String, Object> analytics = analyticsService.getBookAnalytics(bookId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get predictive analytics for library operations
     * Only accessible by admins
     *
     * @return A map containing predictive analytics
     */
    @GetMapping("/predictive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPredictiveAnalytics() {
        Map<String, Object> predictions = analyticsService.getPredictiveAnalytics();
        return ResponseEntity.ok(predictions);
    }
}
