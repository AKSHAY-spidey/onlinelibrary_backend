package com.library.service.impl;

import com.library.exception.ResourceNotFoundException;
import com.library.model.Subscription;
import com.library.model.SubscriptionPlan;
import com.library.model.User;
import com.library.payload.request.SubscriptionPlanRequest;
import com.library.payload.request.SubscriptionRequest;
import com.library.payload.response.SubscriptionPlanResponse;
import com.library.payload.response.SubscriptionResponse;
import com.library.repository.SubscriptionPlanRepository;
import com.library.repository.SubscriptionRepository;
import com.library.repository.UserRepository;
import com.library.security.services.UserDetailsImpl;
import com.library.service.BrevoEmailService;
import com.library.service.EmailService;
import com.library.service.NotificationService;
import com.library.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BrevoEmailService brevoEmailService;

    // Default values for regular users
    private static final int DEFAULT_LOAN_DURATION = 14; // 14 days
    private static final int DEFAULT_GRACE_PERIOD = 0; // 0 days
    private static final double DEFAULT_FINE_DISCOUNT = 0.0; // 0%
    private static final int DEFAULT_MAX_BOOKS = 3; // 3 books

    @Override
    public List<SubscriptionPlanResponse> getAllPlans() {
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAll();
        return plans.stream()
                .map(SubscriptionPlanResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionPlanResponse> getActivePlans() {
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findByIsActiveOrderByMonthlyPriceAsc(true);
        return plans.stream()
                .map(SubscriptionPlanResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionPlanResponse getPlanById(Long planId) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found"));
        return new SubscriptionPlanResponse(plan);
    }

    @Override
    @Transactional
    public SubscriptionPlanResponse createPlan(SubscriptionPlanRequest planRequest) {
        // Check if a plan with the same name already exists
        if (subscriptionPlanRepository.findByName(planRequest.getName()).isPresent()) {
            throw new IllegalArgumentException("A plan with this name already exists");
        }

        // Convert features list to JSON string
        String featuresJson = "";
        if (planRequest.getFeatures() != null && !planRequest.getFeatures().isEmpty()) {
            featuresJson = planRequest.getFeatures().toString();
        }

        SubscriptionPlan plan = new SubscriptionPlan(
                planRequest.getName(),
                planRequest.getDescription(),
                planRequest.getMonthlyPrice(),
                planRequest.getAnnualPrice(),
                planRequest.getMaxBooks(),
                planRequest.getLoanDuration(),
                planRequest.getGracePeriod(),
                planRequest.getReservationPriority(),
                planRequest.getFineDiscount(),
                planRequest.getIsActive(),
                featuresJson
        );

        SubscriptionPlan savedPlan = subscriptionPlanRepository.save(plan);
        return new SubscriptionPlanResponse(savedPlan);
    }

    @Override
    @Transactional
    public SubscriptionPlanResponse updatePlan(Long planId, SubscriptionPlanRequest planRequest) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found"));

        // Check if name is being changed and if a plan with the new name already exists
        if (!plan.getName().equals(planRequest.getName()) &&
                subscriptionPlanRepository.findByName(planRequest.getName()).isPresent()) {
            throw new IllegalArgumentException("A plan with this name already exists");
        }

        // Convert features list to JSON string
        String featuresJson = "";
        if (planRequest.getFeatures() != null && !planRequest.getFeatures().isEmpty()) {
            featuresJson = planRequest.getFeatures().toString();
        }

        plan.setName(planRequest.getName());
        plan.setDescription(planRequest.getDescription());
        plan.setMonthlyPrice(planRequest.getMonthlyPrice());
        plan.setAnnualPrice(planRequest.getAnnualPrice());
        plan.setMaxBooks(planRequest.getMaxBooks());
        plan.setLoanDuration(planRequest.getLoanDuration());
        plan.setGracePeriod(planRequest.getGracePeriod());
        plan.setReservationPriority(planRequest.getReservationPriority());
        plan.setFineDiscount(planRequest.getFineDiscount());
        plan.setIsActive(planRequest.getIsActive());
        plan.setFeatures(featuresJson);

        SubscriptionPlan updatedPlan = subscriptionPlanRepository.save(plan);
        return new SubscriptionPlanResponse(updatedPlan);
    }

    @Override
    @Transactional
    public boolean deletePlan(Long planId) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found"));

        // Check if there are active subscriptions for this plan
        List<Subscription> activeSubscriptions = subscriptionRepository.findByStatus("ACTIVE");
        boolean hasActiveSubscriptions = activeSubscriptions.stream()
                .anyMatch(s -> s.getPlanType().equals(plan.getName()));

        if (hasActiveSubscriptions) {
            throw new IllegalStateException("Cannot delete plan with active subscriptions");
        }

        subscriptionPlanRepository.delete(plan);
        return true;
    }

    @Override
    public SubscriptionResponse getCurrentSubscription(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Subscription> activeSubscription = subscriptionRepository.findActiveSubscriptionByUser(user, LocalDateTime.now());
        return activeSubscription.map(SubscriptionResponse::new).orElse(null);
    }

    @Override
    @Transactional
    public SubscriptionResponse subscribe(Authentication authentication, SubscriptionRequest subscriptionRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscriptionRequest.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found"));

        if (!plan.getIsActive()) {
            throw new IllegalArgumentException("This subscription plan is not active");
        }

        // Verify payment method and payment ID
        if (subscriptionRequest.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }

        // For paid plans, require payment verification
        if ((plan.getMonthlyPrice() > 0 || plan.getAnnualPrice() > 0) &&
            !"FREE".equals(plan.getName()) &&
            ("RAZORPAY".equals(subscriptionRequest.getPaymentMethod()) &&
             (subscriptionRequest.getPaymentId() == null || subscriptionRequest.getPaymentId().isEmpty()))) {
            throw new IllegalArgumentException("Payment verification failed. Please complete the payment process.");
        }

        // Check if user already has an active subscription
        Optional<Subscription> existingSubscription = subscriptionRepository.findActiveSubscriptionByUser(user, LocalDateTime.now());
        if (existingSubscription.isPresent()) {
            // Cancel the existing subscription
            Subscription existing = existingSubscription.get();
            existing.setStatus("CANCELLED");
            subscriptionRepository.save(existing);
        }

        // Calculate subscription period
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate;
        double price;

        if ("MONTHLY".equals(subscriptionRequest.getDuration())) {
            endDate = startDate.plusMonths(1);
            price = plan.getMonthlyPrice();
        } else if ("ANNUAL".equals(subscriptionRequest.getDuration())) {
            endDate = startDate.plusYears(1);
            price = plan.getAnnualPrice() != null ? plan.getAnnualPrice() : plan.getMonthlyPrice() * 12;
        } else {
            throw new IllegalArgumentException("Invalid subscription duration");
        }

        // Create new subscription with payment details
        Subscription subscription = new Subscription(
                user,
                plan.getName(),
                startDate,
                endDate,
                subscriptionRequest.getAutoRenew(),
                subscriptionRequest.getPaymentId(),
                subscriptionRequest.getOrderId(),
                subscriptionRequest.getSignature(),
                "ACTIVE",
                price,
                plan.getMaxBooks(),
                plan.getLoanDuration(),
                plan.getGracePeriod(),
                plan.getReservationPriority(),
                plan.getFineDiscount()
        );

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // Send notification to user
        notificationService.sendUserNotification(
                user.getId(),
                "SUBSCRIPTION_ACTIVATED",
                "Your " + plan.getName() + " subscription has been activated.",
                savedSubscription
        );

        // Send email to user
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            // Send enhanced email with our new template
            brevoEmailService.sendSubscriptionActivationEmail(
                    user.getEmail(),
                    user.getUsername(),
                    plan.getName(),
                    endDate.toLocalDate()
            );
        }

        return new SubscriptionResponse(savedSubscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse cancelSubscription(Authentication authentication, Long subscriptionId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        // Check if the subscription belongs to the user
        if (!subscription.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to cancel this subscription");
        }

        // Check if the subscription is already cancelled or expired
        if (!"ACTIVE".equals(subscription.getStatus())) {
            throw new IllegalStateException("This subscription is not active");
        }

        // Cancel the subscription
        subscription.setStatus("CANCELLED");
        subscription.setAutoRenew(false);
        Subscription cancelledSubscription = subscriptionRepository.save(subscription);

        // Send notification to user
        notificationService.sendUserNotification(
                user.getId(),
                "SUBSCRIPTION_CANCELLED",
                "Your " + subscription.getPlanType() + " subscription has been cancelled.",
                cancelledSubscription
        );

        // Send email to user
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            // Send enhanced email with our new template
            brevoEmailService.sendSubscriptionCancellationEmail(
                    user.getEmail(),
                    user.getUsername(),
                    subscription.getPlanType(),
                    subscription.getEndDate().toLocalDate()
            );

            // Also send the legacy email for backward compatibility
            emailService.sendAccountStatusEmail(
                    user.getEmail(),
                    user.getUsername(),
                    "subscription_cancelled",
                    "Your " + subscription.getPlanType() + " subscription has been cancelled."
            );
        }

        return new SubscriptionResponse(cancelledSubscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse changePlan(Authentication authentication, Long subscriptionId, SubscriptionRequest subscriptionRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        // Check if the subscription belongs to the user
        if (!subscription.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to modify this subscription");
        }

        // Check if the subscription is active
        if (!"ACTIVE".equals(subscription.getStatus())) {
            throw new IllegalStateException("This subscription is not active");
        }

        SubscriptionPlan newPlan = subscriptionPlanRepository.findById(subscriptionRequest.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found"));

        if (!newPlan.getIsActive()) {
            throw new IllegalArgumentException("The selected plan is not active");
        }

        // Verify payment method and payment ID
        if (subscriptionRequest.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }

        // For paid plans, require payment verification
        if ((newPlan.getMonthlyPrice() > 0 || newPlan.getAnnualPrice() > 0) &&
            !"FREE".equals(newPlan.getName()) &&
            ("RAZORPAY".equals(subscriptionRequest.getPaymentMethod()) &&
             (subscriptionRequest.getPaymentId() == null || subscriptionRequest.getPaymentId().isEmpty()))) {
            throw new IllegalArgumentException("Payment verification failed. Please complete the payment process.");
        }

        // Calculate new subscription period
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate;
        double price;

        if ("MONTHLY".equals(subscriptionRequest.getDuration())) {
            endDate = startDate.plusMonths(1);
            price = newPlan.getMonthlyPrice();
        } else if ("ANNUAL".equals(subscriptionRequest.getDuration())) {
            endDate = startDate.plusYears(1);
            price = newPlan.getAnnualPrice() != null ? newPlan.getAnnualPrice() : newPlan.getMonthlyPrice() * 12;
        } else {
            throw new IllegalArgumentException("Invalid subscription duration");
        }

        // Get the current plan type for notifications
        String oldPlanType = subscription.getPlanType();

        // Determine if this is an upgrade or downgrade
        boolean isUpgrade = false;
        boolean isDowngrade = false;

        // Compare benefits to determine if it's an upgrade or downgrade
        if (newPlan.getMaxBooks() > subscription.getMaxBooks() ||
            newPlan.getLoanDuration() > subscription.getLoanDuration() ||
            newPlan.getGracePeriod() > subscription.getGracePeriod() ||
            newPlan.getFineDiscount() > subscription.getFineDiscount()) {
            isUpgrade = true;
        } else if (newPlan.getMaxBooks() < subscription.getMaxBooks() ||
                  newPlan.getLoanDuration() < subscription.getLoanDuration() ||
                  newPlan.getGracePeriod() < subscription.getGracePeriod() ||
                  newPlan.getFineDiscount() < subscription.getFineDiscount()) {
            isDowngrade = true;
        }

        // Cancel the current subscription
        subscription.setStatus("CANCELLED");
        subscription.setAutoRenew(false);
        subscriptionRepository.save(subscription);

        // Create new subscription with payment details
        Subscription newSubscription = new Subscription(
                user,
                newPlan.getName(),
                startDate,
                endDate,
                subscriptionRequest.getAutoRenew(),
                subscriptionRequest.getPaymentId(),
                subscriptionRequest.getOrderId(),
                subscriptionRequest.getSignature(),
                "ACTIVE",
                price,
                newPlan.getMaxBooks(),
                newPlan.getLoanDuration(),
                newPlan.getGracePeriod(),
                newPlan.getReservationPriority(),
                newPlan.getFineDiscount()
        );

        Subscription savedSubscription = subscriptionRepository.save(newSubscription);

        // Prepare notification message based on upgrade/downgrade status
        String notificationType = "SUBSCRIPTION_CHANGED";
        String notificationMessage;
        String emailTemplate = "subscription_changed";
        String emailMessage;

        if (isUpgrade) {
            notificationType = "SUBSCRIPTION_UPGRADED";
            notificationMessage = "Your subscription has been upgraded from " + oldPlanType + " to " + newPlan.getName() + ".";
            emailTemplate = "subscription_upgraded";
            emailMessage = "Your subscription has been upgraded from " + oldPlanType + " to " + newPlan.getName() +
                          " and will be valid until " + endDate.toLocalDate().toString() + ".";
        } else if (isDowngrade) {
            notificationType = "SUBSCRIPTION_DOWNGRADED";
            notificationMessage = "Your subscription has been downgraded from " + oldPlanType + " to " + newPlan.getName() + ".";
            emailTemplate = "subscription_downgraded";
            emailMessage = "Your subscription has been downgraded from " + oldPlanType + " to " + newPlan.getName() +
                          " and will be valid until " + endDate.toLocalDate().toString() + ".";
        } else {
            notificationMessage = "Your subscription has been changed to " + newPlan.getName() + ".";
            emailMessage = "Your subscription has been changed to " + newPlan.getName() +
                          " and will be valid until " + endDate.toLocalDate().toString() + ".";
        }

        // Send notification to user
        notificationService.sendUserNotification(
                user.getId(),
                notificationType,
                notificationMessage,
                savedSubscription
        );

        // Send email to user
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            // Send enhanced email with our new template
            brevoEmailService.sendSubscriptionChangeEmail(
                    user.getEmail(),
                    user.getUsername(),
                    oldPlanType,
                    newPlan.getName(),
                    isUpgrade,
                    endDate.toLocalDate()
            );

            // Also send the legacy email for backward compatibility
            emailService.sendAccountStatusEmail(
                    user.getEmail(),
                    user.getUsername(),
                    emailTemplate,
                    emailMessage
            );
        }

        return new SubscriptionResponse(savedSubscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse toggleAutoRenew(Authentication authentication, Long subscriptionId, Boolean autoRenew) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        // Check if the subscription belongs to the user
        if (!subscription.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to modify this subscription");
        }

        // Check if the subscription is active
        if (!"ACTIVE".equals(subscription.getStatus())) {
            throw new IllegalStateException("This subscription is not active");
        }

        // Toggle auto-renew
        subscription.setAutoRenew(autoRenew);
        Subscription updatedSubscription = subscriptionRepository.save(subscription);

        // Send notification to user
        String message = autoRenew ?
                "Auto-renewal has been enabled for your " + subscription.getPlanType() + " subscription." :
                "Auto-renewal has been disabled for your " + subscription.getPlanType() + " subscription.";

        notificationService.sendUserNotification(
                user.getId(),
                "SUBSCRIPTION_UPDATED",
                message,
                updatedSubscription
        );

        return new SubscriptionResponse(updatedSubscription);
    }

    @Override
    public List<SubscriptionResponse> getUserSubscriptions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Subscription> subscriptions = subscriptionRepository.findByUser(user);
        return subscriptions.stream()
                .map(SubscriptionResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionResponse> getAllActiveSubscriptions() {
        List<Subscription> activeSubscriptions = subscriptionRepository.findByStatus("ACTIVE");
        return activeSubscriptions.stream()
                .map(SubscriptionResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getSubscriptionStats() {
        Map<String, Object> stats = new HashMap<>();

        // Get total active subscriptions
        List<Subscription> activeSubscriptions = subscriptionRepository.findByStatus("ACTIVE");
        stats.put("totalActiveSubscriptions", activeSubscriptions.size());

        // Get subscriptions by plan type
        List<Object[]> subscriptionsByPlanType = subscriptionRepository.countActiveSubscriptionsByPlanTypeGrouped();
        Map<String, Long> planTypeCounts = new HashMap<>();
        for (Object[] result : subscriptionsByPlanType) {
            planTypeCounts.put((String) result[0], (Long) result[1]);
        }
        stats.put("subscriptionsByPlanType", planTypeCounts);

        // Get monthly vs annual subscriptions
        long monthlyCount = activeSubscriptions.stream()
                .filter(s -> java.time.Duration.between(s.getStartDate(), s.getEndDate()).toDays() <= 32)
                .count();
        long annualCount = activeSubscriptions.size() - monthlyCount;
        stats.put("monthlySubscriptions", monthlyCount);
        stats.put("annualSubscriptions", annualCount);

        // Get auto-renew stats
        long autoRenewCount = activeSubscriptions.stream()
                .filter(Subscription::getAutoRenew)
                .count();
        stats.put("autoRenewEnabled", autoRenewCount);
        stats.put("autoRenewPercentage", activeSubscriptions.isEmpty() ? 0 : (double) autoRenewCount / activeSubscriptions.size() * 100);

        return stats;
    }

    @Override
    @Transactional
    public void processRenewals() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime renewalWindow = now.plusDays(3); // Process subscriptions expiring in the next 3 days

        List<Subscription> subscriptionsToRenew = subscriptionRepository.findByAutoRenewAndEndDateBetween(
                true, now, renewalWindow);

        for (Subscription subscription : subscriptionsToRenew) {
            try {
                // Get the subscription plan
                Optional<SubscriptionPlan> planOpt = subscriptionPlanRepository.findByName(subscription.getPlanType());
                if (planOpt.isEmpty() || !planOpt.get().getIsActive()) {
                    // Plan no longer exists or is inactive, cancel auto-renewal
                    subscription.setAutoRenew(false);
                    subscriptionRepository.save(subscription);
                    continue;
                }

                SubscriptionPlan plan = planOpt.get();

                // Calculate new subscription period
                LocalDateTime newStartDate = subscription.getEndDate();
                LocalDateTime newEndDate;
                double price;

                // Determine if it's a monthly or annual subscription
                long durationDays = java.time.Duration.between(subscription.getStartDate(), subscription.getEndDate()).toDays();
                if (durationDays <= 32) { // Monthly
                    newEndDate = newStartDate.plusMonths(1);
                    price = plan.getMonthlyPrice();
                } else { // Annual
                    newEndDate = newStartDate.plusYears(1);
                    price = plan.getAnnualPrice() != null ? plan.getAnnualPrice() : plan.getMonthlyPrice() * 12;
                }

                // Create new subscription
                Subscription newSubscription = new Subscription(
                        subscription.getUser(),
                        plan.getName(),
                        newStartDate,
                        newEndDate,
                        subscription.getAutoRenew(),
                        null, // Payment ID will be updated after payment processing
                        null, // Order ID will be updated after payment processing
                        null, // Signature will be updated after payment processing
                        "PENDING", // Set to pending until payment is confirmed
                        price,
                        plan.getMaxBooks(),
                        plan.getLoanDuration(),
                        plan.getGracePeriod(),
                        plan.getReservationPriority(),
                        plan.getFineDiscount()
                );

                subscriptionRepository.save(newSubscription);

                // Mark old subscription as renewed
                subscription.setStatus("RENEWED");
                subscriptionRepository.save(subscription);

                // Send notification to user
                notificationService.sendUserNotification(
                        subscription.getUser().getId(),
                        "SUBSCRIPTION_RENEWAL",
                        "Your " + plan.getName() + " subscription has been renewed.",
                        newSubscription
                );

                // Send email to user
                if (subscription.getUser().getEmail() != null && !subscription.getUser().getEmail().isEmpty()) {
                    // Send enhanced email with our new template
                    brevoEmailService.sendSubscriptionActivationEmail(
                            subscription.getUser().getEmail(),
                            subscription.getUser().getUsername(),
                            plan.getName(),
                            newEndDate.toLocalDate()
                    );

                    // Also send the legacy email for backward compatibility
                    emailService.sendAccountStatusEmail(
                            subscription.getUser().getEmail(),
                            subscription.getUser().getUsername(),
                            "subscription_renewed",
                            "Your " + plan.getName() + " subscription has been renewed and will be valid until " + newEndDate.toLocalDate().toString() + "."
                    );
                }

                // TODO: Process payment for the renewal
                // This would typically involve integrating with a payment gateway

            } catch (Exception e) {
                // Log the error and continue with the next subscription
                System.err.println("Error processing renewal for subscription ID " + subscription.getId() + ": " + e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void processExpiredSubscriptions() {
        LocalDateTime now = LocalDateTime.now();

        List<Subscription> expiredSubscriptions = subscriptionRepository.findByEndDateBeforeAndStatus(now, "ACTIVE");

        for (Subscription subscription : expiredSubscriptions) {
            // Mark as expired
            subscription.setStatus("EXPIRED");
            subscriptionRepository.save(subscription);

            // Send notification to user
            notificationService.sendUserNotification(
                    subscription.getUser().getId(),
                    "SUBSCRIPTION_EXPIRED",
                    "Your " + subscription.getPlanType() + " subscription has expired.",
                    subscription
            );

            // Send email to user
            if (subscription.getUser().getEmail() != null && !subscription.getUser().getEmail().isEmpty()) {
                emailService.sendAccountStatusEmail(
                        subscription.getUser().getEmail(),
                        subscription.getUser().getUsername(),
                        "subscription_expired",
                        "Your " + subscription.getPlanType() + " subscription has expired."
                );
            }
        }
    }

    @Override
    public boolean hasActivePremiumSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Subscription> activeSubscription = subscriptionRepository.findActiveSubscriptionByUser(user, LocalDateTime.now());

        return activeSubscription.isPresent() &&
               !"BASIC".equals(activeSubscription.get().getPlanType());
    }

    @Override
    public int getLoanDurationForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Subscription> activeSubscription = subscriptionRepository.findActiveSubscriptionByUser(user, LocalDateTime.now());

        return activeSubscription.map(Subscription::getLoanDuration)
                .orElse(DEFAULT_LOAN_DURATION);
    }

    @Override
    public int getGracePeriodForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Subscription> activeSubscription = subscriptionRepository.findActiveSubscriptionByUser(user, LocalDateTime.now());

        return activeSubscription.map(Subscription::getGracePeriod)
                .orElse(DEFAULT_GRACE_PERIOD);
    }

    @Override
    public double getFineDiscountForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Subscription> activeSubscription = subscriptionRepository.findActiveSubscriptionByUser(user, LocalDateTime.now());

        return activeSubscription.map(Subscription::getFineDiscount)
                .orElse(DEFAULT_FINE_DISCOUNT);
    }

    @Override
    public int getMaxBooksForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Subscription> activeSubscription = subscriptionRepository.findActiveSubscriptionByUser(user, LocalDateTime.now());

        return activeSubscription.map(Subscription::getMaxBooks)
                .orElse(DEFAULT_MAX_BOOKS);
    }
}
