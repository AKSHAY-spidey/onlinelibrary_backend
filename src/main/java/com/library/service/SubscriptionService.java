package com.library.service;

import com.library.model.Subscription;
import com.library.model.SubscriptionPlan;
import com.library.payload.request.SubscriptionPlanRequest;
import com.library.payload.request.SubscriptionRequest;
import com.library.payload.response.SubscriptionPlanResponse;
import com.library.payload.response.SubscriptionResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface SubscriptionService {

    /**
     * Get all subscription plans
     *
     * @return List of subscription plan responses
     */
    List<SubscriptionPlanResponse> getAllPlans();

    /**
     * Get active subscription plans
     *
     * @return List of active subscription plan responses
     */
    List<SubscriptionPlanResponse> getActivePlans();

    /**
     * Get a subscription plan by ID
     *
     * @param planId The ID of the plan
     * @return The subscription plan response
     */
    SubscriptionPlanResponse getPlanById(Long planId);

    /**
     * Create a new subscription plan (admin only)
     *
     * @param planRequest The subscription plan request
     * @return The created subscription plan response
     */
    SubscriptionPlanResponse createPlan(SubscriptionPlanRequest planRequest);

    /**
     * Update a subscription plan (admin only)
     *
     * @param planId The ID of the plan to update
     * @param planRequest The subscription plan request
     * @return The updated subscription plan response
     */
    SubscriptionPlanResponse updatePlan(Long planId, SubscriptionPlanRequest planRequest);

    /**
     * Delete a subscription plan (admin only)
     *
     * @param planId The ID of the plan to delete
     * @return True if deleted successfully
     */
    boolean deletePlan(Long planId);

    /**
     * Get the current user's active subscription
     *
     * @param authentication The authenticated user
     * @return The subscription response or null if no active subscription
     */
    SubscriptionResponse getCurrentSubscription(Authentication authentication);

    /**
     * Subscribe to a plan
     *
     * @param authentication The authenticated user
     * @param subscriptionRequest The subscription request
     * @return The subscription response
     */
    SubscriptionResponse subscribe(Authentication authentication, SubscriptionRequest subscriptionRequest);

    /**
     * Change subscription plan
     *
     * @param authentication The authenticated user
     * @param subscriptionId The ID of the subscription to change
     * @param subscriptionRequest The new subscription request
     * @return The updated subscription response
     */
    SubscriptionResponse changePlan(Authentication authentication, Long subscriptionId, SubscriptionRequest subscriptionRequest);

    /**
     * Cancel a subscription
     *
     * @param authentication The authenticated user
     * @param subscriptionId The ID of the subscription to cancel
     * @return The updated subscription response
     */
    SubscriptionResponse cancelSubscription(Authentication authentication, Long subscriptionId);

    /**
     * Toggle auto-renew for a subscription
     *
     * @param authentication The authenticated user
     * @param subscriptionId The ID of the subscription
     * @param autoRenew Whether to auto-renew
     * @return The updated subscription response
     */
    SubscriptionResponse toggleAutoRenew(Authentication authentication, Long subscriptionId, Boolean autoRenew);

    /**
     * Get all subscriptions for a user (admin only)
     *
     * @param userId The ID of the user
     * @return List of subscription responses
     */
    List<SubscriptionResponse> getUserSubscriptions(Long userId);

    /**
     * Get all active subscriptions (admin only)
     *
     * @return List of active subscription responses
     */
    List<SubscriptionResponse> getAllActiveSubscriptions();

    /**
     * Get subscription statistics (admin only)
     *
     * @return Map of statistics
     */
    java.util.Map<String, Object> getSubscriptionStats();

    /**
     * Process subscription renewals (scheduled task)
     */
    void processRenewals();

    /**
     * Process expired subscriptions (scheduled task)
     */
    void processExpiredSubscriptions();

    /**
     * Check if a user has an active premium subscription
     *
     * @param userId The ID of the user
     * @return True if the user has an active premium subscription
     */
    boolean hasActivePremiumSubscription(Long userId);

    /**
     * Get the loan duration for a user based on their subscription
     *
     * @param userId The ID of the user
     * @return The loan duration in days
     */
    int getLoanDurationForUser(Long userId);

    /**
     * Get the grace period for a user based on their subscription
     *
     * @param userId The ID of the user
     * @return The grace period in days
     */
    int getGracePeriodForUser(Long userId);

    /**
     * Get the fine discount for a user based on their subscription
     *
     * @param userId The ID of the user
     * @return The fine discount as a percentage
     */
    double getFineDiscountForUser(Long userId);

    /**
     * Get the maximum number of books a user can borrow based on their subscription
     *
     * @param userId The ID of the user
     * @return The maximum number of books
     */
    int getMaxBooksForUser(Long userId);
}
