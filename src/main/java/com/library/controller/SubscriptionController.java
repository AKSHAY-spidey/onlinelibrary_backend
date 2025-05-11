package com.library.controller;

import com.library.payload.request.SubscriptionPlanRequest;
import com.library.payload.request.SubscriptionRequest;
import com.library.payload.response.MessageResponse;
import com.library.payload.response.SubscriptionPlanResponse;
import com.library.payload.response.SubscriptionResponse;
import com.library.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    // Public endpoints
    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlanResponse>> getActivePlans() {
        List<SubscriptionPlanResponse> plans = subscriptionService.getActivePlans();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/plans/{planId}")
    public ResponseEntity<SubscriptionPlanResponse> getPlanById(@PathVariable Long planId) {
        SubscriptionPlanResponse plan = subscriptionService.getPlanById(planId);
        return ResponseEntity.ok(plan);
    }

    // User endpoints
    @GetMapping("/current")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> getCurrentSubscription(Authentication authentication) {
        SubscriptionResponse subscription = subscriptionService.getCurrentSubscription(authentication);
        if (subscription == null) {
            return ResponseEntity.ok(new MessageResponse("No active subscription found"));
        }
        return ResponseEntity.ok(subscription);
    }

    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponse> subscribe(
            Authentication authentication,
            @Valid @RequestBody SubscriptionRequest subscriptionRequest) {
        SubscriptionResponse subscription = subscriptionService.subscribe(authentication, subscriptionRequest);
        return ResponseEntity.ok(subscription);
    }

    @PostMapping("/{subscriptionId}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
            Authentication authentication,
            @PathVariable Long subscriptionId) {
        SubscriptionResponse subscription = subscriptionService.cancelSubscription(authentication, subscriptionId);
        return ResponseEntity.ok(subscription);
    }

    @PostMapping("/{subscriptionId}/change-plan")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponse> changePlan(
            Authentication authentication,
            @PathVariable Long subscriptionId,
            @Valid @RequestBody SubscriptionRequest subscriptionRequest) {
        SubscriptionResponse subscription = subscriptionService.changePlan(authentication, subscriptionId, subscriptionRequest);
        return ResponseEntity.ok(subscription);
    }

    @PostMapping("/{subscriptionId}/auto-renew")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponse> toggleAutoRenew(
            Authentication authentication,
            @PathVariable Long subscriptionId,
            @RequestParam Boolean autoRenew) {
        SubscriptionResponse subscription = subscriptionService.toggleAutoRenew(authentication, subscriptionId, autoRenew);
        return ResponseEntity.ok(subscription);
    }

    // Admin endpoints
    @GetMapping("/admin/plans")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubscriptionPlanResponse>> getAllPlans() {
        List<SubscriptionPlanResponse> plans = subscriptionService.getAllPlans();
        return ResponseEntity.ok(plans);
    }

    @PostMapping("/admin/plans")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionPlanResponse> createPlan(@Valid @RequestBody SubscriptionPlanRequest planRequest) {
        SubscriptionPlanResponse plan = subscriptionService.createPlan(planRequest);
        return ResponseEntity.ok(plan);
    }

    @PutMapping("/admin/plans/{planId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionPlanResponse> updatePlan(
            @PathVariable Long planId,
            @Valid @RequestBody SubscriptionPlanRequest planRequest) {
        SubscriptionPlanResponse plan = subscriptionService.updatePlan(planId, planRequest);
        return ResponseEntity.ok(plan);
    }

    @DeleteMapping("/admin/plans/{planId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deletePlan(@PathVariable Long planId) {
        boolean deleted = subscriptionService.deletePlan(planId);
        if (deleted) {
            return ResponseEntity.ok(new MessageResponse("Subscription plan deleted successfully"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Failed to delete subscription plan"));
        }
    }

    @GetMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubscriptionResponse>> getUserSubscriptions(@PathVariable Long userId) {
        List<SubscriptionResponse> subscriptions = subscriptionService.getUserSubscriptions(userId);
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/admin/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubscriptionResponse>> getAllActiveSubscriptions() {
        List<SubscriptionResponse> subscriptions = subscriptionService.getAllActiveSubscriptions();
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSubscriptionStats() {
        Map<String, Object> stats = subscriptionService.getSubscriptionStats();
        return ResponseEntity.ok(stats);
    }

    // Utility endpoints
    @GetMapping("/check-premium/{userId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Boolean> checkPremiumStatus(@PathVariable Long userId) {
        boolean isPremium = subscriptionService.hasActivePremiumSubscription(userId);
        return ResponseEntity.ok(isPremium);
    }

    @GetMapping("/loan-duration/{userId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Integer> getLoanDuration(@PathVariable Long userId) {
        int loanDuration = subscriptionService.getLoanDurationForUser(userId);
        return ResponseEntity.ok(loanDuration);
    }

    @GetMapping("/grace-period/{userId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Integer> getGracePeriod(@PathVariable Long userId) {
        int gracePeriod = subscriptionService.getGracePeriodForUser(userId);
        return ResponseEntity.ok(gracePeriod);
    }

    @GetMapping("/fine-discount/{userId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Double> getFineDiscount(@PathVariable Long userId) {
        double fineDiscount = subscriptionService.getFineDiscountForUser(userId);
        return ResponseEntity.ok(fineDiscount);
    }

    @GetMapping("/max-books/{userId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Integer> getMaxBooks(@PathVariable Long userId) {
        int maxBooks = subscriptionService.getMaxBooksForUser(userId);
        return ResponseEntity.ok(maxBooks);
    }
}
