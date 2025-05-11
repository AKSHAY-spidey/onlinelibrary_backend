package com.library.service.impl;

import com.library.exception.ResourceNotFoundException;
import com.library.model.Loan;
import com.library.model.Payment;
import com.library.model.PaymentMethod;
import com.library.model.Subscription;
import com.library.model.SubscriptionPlan;
import com.library.model.User;
import com.library.payload.request.PaymentRequest;
import com.library.payload.request.RazorpayCallbackRequest;
import com.library.payload.request.SubscriptionRequest;
import com.library.payload.response.PaymentMethodResponse;
import com.library.payload.response.PaymentResponse;
import com.library.payload.response.RazorpayOrderResponse;
import com.library.repository.LoanRepository;
import com.library.repository.PaymentMethodRepository;
import com.library.repository.PaymentRepository;
import com.library.repository.SubscriptionPlanRepository;
import com.library.repository.SubscriptionRepository;
import com.library.repository.UserRepository;
import com.library.security.services.UserDetailsImpl;
import com.library.service.BlockchainService;
import com.library.service.EmailService;
import com.library.service.NotificationService;
import com.library.service.PaymentService;
import com.library.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private SubscriptionService subscriptionService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${app.name}")
    private String appName;

    @Value("${app.logo.url}")
    private String appLogoUrl;

    @Override
    @Transactional
    public RazorpayOrderResponse createRazorpayOrder(Authentication authentication, Long loanId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        // Check if the loan belongs to the user
        if (!loan.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to pay for this loan");
        }

        // Check if the loan has a fine
        if (loan.getFineAmount() == null || loan.getFineAmount() <= 0) {
            throw new IllegalStateException("This loan does not have any fine to pay");
        }

        // Check if the fine has already been paid
        if (paymentRepository.existsByLoanAndStatus(loan, "COMPLETED")) {
            throw new IllegalStateException("The fine for this loan has already been paid");
        }

        // In a real implementation, we would use the Razorpay API to create an order
        // For this example, we'll create a mock order
        String orderId = "order_" + System.currentTimeMillis();
        String receipt = "rcpt_" + System.currentTimeMillis();

        // Convert amount to paise (Razorpay uses smallest currency unit)
        Integer amountInPaise = (int) (loan.getFineAmount() * 100);

        // Create a payment record in PENDING status
        Payment payment = new Payment(
                user,
                loan,
                loan.getFineAmount(),
                "RAZORPAY",
                orderId,
                LocalDateTime.now(),
                "PENDING",
                "Fine payment for overdue book: " + loan.getBook().getTitle()
        );

        paymentRepository.save(payment);

        // Create the Razorpay order response
        RazorpayOrderResponse response = new RazorpayOrderResponse();
        response.setOrderId(orderId);
        response.setCurrency("INR");
        response.setAmount(amountInPaise);
        response.setReceipt(receipt);
        response.setKey(razorpayKeyId);
        response.setName(appName);
        response.setDescription("Fine payment for overdue book: " + loan.getBook().getTitle());
        response.setImage(appLogoUrl);
        response.setPrefillName(user.getUsername());
        response.setPrefillEmail(user.getEmail());
        response.setPrefillContact("");
        response.setTheme("#4a90e2");

        return response;
    }

    @Override
    @Transactional
    public RazorpayOrderResponse createFinePaymentOrder(Authentication authentication, Long loanId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        // Check if the loan belongs to the user
        if (!loan.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to pay for this loan");
        }

        // Check if the loan has a fine
        if (loan.getFineAmount() == null || loan.getFineAmount() <= 0) {
            throw new IllegalStateException("This loan does not have any fine to pay");
        }

        // Check if the fine has already been paid
        if (paymentRepository.existsByLoanAndStatus(loan, "COMPLETED")) {
            throw new IllegalStateException("The fine for this loan has already been paid");
        }

        // Create a unique order ID and receipt
        String orderId = "fine_order_" + System.currentTimeMillis();
        String receipt = "fine_rcpt_" + loan.getId() + "_" + System.currentTimeMillis();

        // Convert amount to paise (Razorpay uses smallest currency unit)
        Integer amountInPaise = (int) (loan.getFineAmount() * 100);

        // Ensure minimum amount for Razorpay (100 paise = 1 rupee)
        if (amountInPaise < 100) {
            amountInPaise = 100; // Minimum 1 rupee
        }

        // Create a payment record in PENDING status
        Payment payment = new Payment(
                user,
                loan,
                loan.getFineAmount(),
                "RAZORPAY",
                orderId,
                LocalDateTime.now(),
                "PENDING",
                "Fine payment for overdue book: " + loan.getBook().getTitle()
        );

        payment.setReceiptNumber("FINE-" + loan.getId() + "-" + System.currentTimeMillis());
        Payment savedPayment = paymentRepository.save(payment);

        // Create the Razorpay order response
        RazorpayOrderResponse response = new RazorpayOrderResponse();
        response.setOrderId(orderId);
        response.setCurrency("INR");
        response.setAmount(amountInPaise);
        response.setReceipt(receipt);
        response.setKey(razorpayKeyId);
        response.setName(appName);
        response.setDescription("Fine Payment - " + loan.getBook().getTitle());
        response.setImage(appLogoUrl);
        response.setPrefillName(user.getUsername());
        response.setPrefillEmail(user.getEmail());
        response.setPrefillContact("");
        response.setTheme("#e74c3c"); // Red theme for fines

        // Send notification to user about fine payment
        notificationService.sendUserNotification(
                user.getId(),
                "FINE_PAYMENT_INITIATED",
                "Fine payment of ₹" + loan.getFineAmount() + " initiated for overdue book: " + loan.getBook().getTitle(),
                savedPayment
        );

        // Send email notification
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            emailService.sendPaymentEmail(
                user.getEmail(),
                user.getUsername(),
                "fine_payment_initiated",
                "Fine Payment Initiated",
                loan.getBook().getTitle(),
                loan.getFineAmount(),
                "INR",
                payment.getReceiptNumber()
            );
        }

        return response;
    }

    @Override
    @Transactional
    public PaymentResponse processRazorpayCallback(Authentication authentication, RazorpayCallbackRequest callbackRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Find the payment by order ID
        Payment payment = paymentRepository.findByTransactionId(callbackRequest.getRazorpayOrderId());

        if (payment == null) {
            throw new ResourceNotFoundException("Payment not found");
        }

        // Check if the payment belongs to the user
        if (!payment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to process this payment");
        }

        // In a real implementation, we would verify the Razorpay signature
        // For this example, we'll assume the payment is successful

        // Update payment status
        payment.setStatus("COMPLETED");
        payment.setTransactionId(callbackRequest.getRazorpayPaymentId());
        payment.setVerified(true);
        payment.setVerifiedBy("SYSTEM");
        payment.setVerificationDate(LocalDateTime.now());

        Payment updatedPayment = paymentRepository.save(payment);

        // Update loan status if this is a fine payment
        if (payment.getLoan() != null) {
            Loan loan = payment.getLoan();
            loan.setFineAmount(0.0); // Clear the fine
            loanRepository.save(loan);

            // Record the payment in blockchain
            blockchainService.recordFinePayment(loan, payment.getAmount());
        }

        // Send notification to user
        notificationService.sendUserNotification(
                user.getId(),
                "PAYMENT_COMPLETED",
                "Your payment of ₹" + payment.getAmount() + " has been completed successfully.",
                updatedPayment
        );

        // Send email receipt
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            emailService.sendPaymentReceiptEmail(
                    user.getEmail(),
                    user.getUsername(),
                    payment.getAmount(),
                    payment.getTransactionId(),
                    payment.getDescription()
            );
        }

        return new PaymentResponse(updatedPayment);
    }

    @Override
    @Transactional
    public PaymentResponse createManualPayment(Authentication authentication, PaymentRequest paymentRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Loan loan = loanRepository.findById(paymentRequest.getLoanId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        // Check if the loan belongs to the user
        if (!loan.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to pay for this loan");
        }

        // Check if the loan has a fine
        if (loan.getFineAmount() == null || loan.getFineAmount() <= 0) {
            throw new IllegalStateException("This loan does not have any fine to pay");
        }

        // Check if the fine has already been paid
        if (paymentRepository.existsByLoanAndStatus(loan, "COMPLETED")) {
            throw new IllegalStateException("The fine for this loan has already been paid");
        }

        // Create a manual payment record
        Payment payment = new Payment(
                user,
                loan,
                paymentRequest.getAmount(),
                paymentRequest.getPaymentMethod(),
                paymentRequest.getTransactionId(),
                LocalDateTime.now(),
                "PENDING", // Manual payments need verification
                paymentRequest.getDescription() != null ? paymentRequest.getDescription() : "Fine payment for overdue book: " + loan.getBook().getTitle()
        );

        if (paymentRequest.getReceiptUrl() != null) {
            payment.setReceiptUrl(paymentRequest.getReceiptUrl());
        }

        Payment savedPayment = paymentRepository.save(payment);

        // Send notification to user
        notificationService.sendUserNotification(
                user.getId(),
                "PAYMENT_PENDING",
                "Your manual payment of ₹" + payment.getAmount() + " is pending verification.",
                savedPayment
        );

        // Send notification to librarians and admins
        notificationService.sendAdminNotification(
                "NEW_MANUAL_PAYMENT",
                "A new manual payment of ₹" + payment.getAmount() + " from " + user.getUsername() + " is pending verification.",
                savedPayment
        );

        return new PaymentResponse(savedPayment);
    }

    @Override
    @Transactional
    public PaymentResponse verifyManualPayment(Authentication authentication, Long paymentId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User admin = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if the user is an admin or librarian
        boolean isAdminOrLibrarian = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LIBRARIAN"));

        if (!isAdminOrLibrarian) {
            throw new AccessDeniedException("You are not authorized to verify payments");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Check if the payment is already verified
        if (payment.getVerified()) {
            throw new IllegalStateException("This payment has already been verified");
        }

        // Verify the payment
        payment.setStatus("COMPLETED");
        payment.setVerified(true);
        payment.setVerifiedBy(admin.getUsername());
        payment.setVerificationDate(LocalDateTime.now());

        Payment updatedPayment = paymentRepository.save(payment);

        // Update loan status if this is a fine payment
        if (payment.getLoan() != null) {
            Loan loan = payment.getLoan();
            loan.setFineAmount(0.0); // Clear the fine
            loanRepository.save(loan);

            // Record the payment in blockchain
            blockchainService.recordFinePayment(loan, payment.getAmount());
        }

        // Send notification to user
        notificationService.sendUserNotification(
                payment.getUser().getId(),
                "PAYMENT_VERIFIED",
                "Your manual payment of ₹" + payment.getAmount() + " has been verified and completed.",
                updatedPayment
        );

        // Send email receipt
        if (payment.getUser().getEmail() != null && !payment.getUser().getEmail().isEmpty()) {
            emailService.sendPaymentReceiptEmail(
                    payment.getUser().getEmail(),
                    payment.getUser().getUsername(),
                    payment.getAmount(),
                    payment.getTransactionId(),
                    payment.getDescription()
            );
        }

        return new PaymentResponse(updatedPayment);
    }

    @Override
    public List<PaymentResponse> getUserPayments(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Payment> payments = paymentRepository.findByUser(user);
        return payments.stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsByStatus(String status) {
        List<Payment> payments = paymentRepository.findByStatus(status);
        return payments.stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Payment> payments = paymentRepository.findByPaymentDateBetween(startDate, endDate);
        return payments.stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getUnverifiedManualPayments() {
        List<Payment> payments = paymentRepository.findByVerified(false);
        return payments.stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse getPaymentById(Authentication authentication, Long paymentId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Check if the user is the owner of the payment or an admin/librarian
        boolean isOwner = payment.getUser().getId().equals(userDetails.getId());
        boolean isAdminOrLibrarian = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LIBRARIAN"));

        if (!isOwner && !isAdminOrLibrarian) {
            throw new AccessDeniedException("You are not authorized to view this payment");
        }

        return new PaymentResponse(payment);
    }

    @Override
    public byte[] generatePaymentReceipt(Authentication authentication, Long paymentId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Check if the user is the owner of the payment or an admin/librarian
        boolean isOwner = payment.getUser().getId().equals(userDetails.getId());
        boolean isAdminOrLibrarian = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_LIBRARIAN"));

        if (!isOwner && !isAdminOrLibrarian) {
            throw new AccessDeniedException("You are not authorized to generate a receipt for this payment");
        }

        // In a real implementation, we would generate a PDF receipt
        // For this example, we'll return a placeholder
        return "Payment Receipt".getBytes();
    }

    @Override
    public RazorpayOrderResponse createSubscriptionOrder(Authentication authentication, SubscriptionRequest subscriptionRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscriptionRequest.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found"));

        if (!plan.getIsActive()) {
            throw new IllegalArgumentException("This subscription plan is not active");
        }

        // Calculate price based on duration or use exactAmount if provided
        double price;

        // Check if exactAmount is provided in the request (from frontend)
        if (subscriptionRequest.getExactAmount() != null && subscriptionRequest.getExactAmount() > 0) {
            // Use the exact amount provided by the frontend (already in paise)
            price = subscriptionRequest.getExactAmount() / 100.0; // Convert back to rupees for our records
            System.out.println("Using exact amount from request: " + price + " rupees (" + subscriptionRequest.getExactAmount() + " paise)");
        } else {
            // Calculate based on plan and duration
            if ("MONTHLY".equals(subscriptionRequest.getDuration())) {
                price = plan.getMonthlyPrice();
            } else if ("ANNUAL".equals(subscriptionRequest.getDuration())) {
                price = plan.getAnnualPrice() != null ? plan.getAnnualPrice() : plan.getMonthlyPrice() * 12 * 0.84;
            } else {
                throw new IllegalArgumentException("Invalid subscription duration");
            }

            // Ensure minimum price for Razorpay
            if (price < 1.0) {
                price = 199.0; // Minimum price to avoid Razorpay errors
                System.out.println("Price was too low, using minimum price: " + price);
            }
        }

        // Create Razorpay order
        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();

            // Use exactAmount directly if provided, otherwise calculate from price
            int amountInPaise;
            if (subscriptionRequest.getExactAmount() != null && subscriptionRequest.getExactAmount() > 0) {
                amountInPaise = subscriptionRequest.getExactAmount();
            } else {
                amountInPaise = (int)(price * 100); // Convert to paise
            }

            // Ensure minimum amount for Razorpay (100 paise = 1 rupee)
            if (amountInPaise < 100) {
                amountInPaise = 19900; // 199 rupees minimum
                System.out.println("Amount was too low, using minimum amount: " + amountInPaise + " paise");
            }

            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "subscription_" + plan.getName() + "_" + user.getId());
            orderRequest.put("payment_capture", 1);

            Order order = razorpay.orders.create(orderRequest);

            RazorpayOrderResponse response = new RazorpayOrderResponse();
            response.setOrderId(order.get("id"));
            response.setAmount((Integer) order.get("amount"));
            response.setCurrency((String) order.get("currency"));
            response.setReceipt((String) order.get("receipt"));
            response.setKey(razorpayKeyId);
            response.setPrefillName(user.getUsername());
            response.setPrefillEmail(user.getEmail());
            response.setPrefillContact(""); // Add phone if available
            response.setName("Online Library");
            response.setDescription("Subscription: " + plan.getName());
            response.setTheme("#4a90e2");

            return response;
        } catch (RazorpayException e) {
            throw new RuntimeException("Error creating Razorpay order: " + e.getMessage(), e);
        }
    }

    @Override
    public RazorpayOrderResponse createSubscriptionChangeOrder(Authentication authentication, SubscriptionRequest subscriptionRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get the current subscription
        Long subscriptionId = subscriptionRequest.getSubscriptionId();
        if (subscriptionId == null) {
            throw new IllegalArgumentException("Subscription ID is required for plan change");
        }

        Subscription currentSubscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Current subscription not found"));

        // Check if the subscription belongs to the user
        if (!currentSubscription.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to change this subscription");
        }

        // Get the new plan
        SubscriptionPlan newPlan = subscriptionPlanRepository.findById(subscriptionRequest.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("New subscription plan not found"));

        if (!newPlan.getIsActive()) {
            throw new IllegalArgumentException("The selected plan is not active");
        }

        // Calculate price difference between current plan and new plan
        double newPrice;
        double currentPrice = currentSubscription.getPrice();
        String planChangeType;

        // Calculate based on plan and duration
        if ("MONTHLY".equals(subscriptionRequest.getDuration())) {
            newPrice = newPlan.getMonthlyPrice();
        } else if ("ANNUAL".equals(subscriptionRequest.getDuration())) {
            newPrice = newPlan.getAnnualPrice() != null ? newPlan.getAnnualPrice() : newPlan.getMonthlyPrice() * 12 * 0.84;
        } else {
            throw new IllegalArgumentException("Invalid subscription duration");
        }

        // Calculate price difference
        double priceDifference = newPrice - currentPrice;

        // Determine if this is an upgrade or downgrade
        if (priceDifference > 0) {
            planChangeType = "upgrade";
        } else if (priceDifference < 0) {
            planChangeType = "downgrade";
            // For downgrades, we'll still create a minimal payment to process the change
            // The actual refund or credit would be handled separately
            priceDifference = 1.0; // Minimum charge of 1 rupee for downgrades
        } else {
            planChangeType = "same";
            // For same price plans, charge a minimal amount
            priceDifference = 1.0;
        }

        // If exactAmount is provided, use that instead
        if (subscriptionRequest.getExactAmount() != null && subscriptionRequest.getExactAmount() > 0) {
            priceDifference = subscriptionRequest.getExactAmount() / 100.0; // Convert paise to rupees
        }

        // Create Razorpay order for the price difference
        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();

            // Convert price difference to paise for Razorpay
            int amountInPaise = (int)(priceDifference * 100);

            // If upgrading to a cheaper or same-priced plan, set a minimum amount
            if (amountInPaise < 100) {
                amountInPaise = 100; // Minimum 1 rupee for Razorpay
            }

            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", planChangeType + "_" + currentSubscription.getPlanType() + "_to_" + newPlan.getName() + "_" + user.getId());
            orderRequest.put("payment_capture", 1);

            Order order = razorpay.orders.create(orderRequest);

            RazorpayOrderResponse response = new RazorpayOrderResponse();
            response.setOrderId(order.get("id"));
            response.setAmount((Integer) order.get("amount"));
            response.setCurrency((String) order.get("currency"));
            response.setReceipt((String) order.get("receipt"));
            response.setKey(razorpayKeyId);
            response.setPrefillName(user.getUsername());
            response.setPrefillEmail(user.getEmail());
            response.setPrefillContact(""); // Add phone if available
            response.setName("Online Library");

            // Set description based on plan change type
            if ("upgrade".equals(planChangeType)) {
                response.setDescription("Upgrade from " + currentSubscription.getPlanType() + " to " + newPlan.getName());
            } else if ("downgrade".equals(planChangeType)) {
                response.setDescription("Downgrade from " + currentSubscription.getPlanType() + " to " + newPlan.getName());
            } else {
                response.setDescription("Change plan from " + currentSubscription.getPlanType() + " to " + newPlan.getName());
            }

            response.setTheme("#4a90e2");

            return response;
        } catch (RazorpayException e) {
            throw new RuntimeException("Error creating Razorpay order for plan change: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentResponse processSubscriptionPayment(Authentication authentication, Long subscriptionId, Long paymentId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        // Check if the subscription belongs to the user
        if (!subscription.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to process this subscription payment");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Check if the payment belongs to the user
        if (!payment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to process this payment");
        }

        // Update subscription status
        subscription.setStatus("ACTIVE");
        subscription.setPaymentId(payment.getTransactionId());
        subscriptionRepository.save(subscription);

        // Send notification to user
        notificationService.sendUserNotification(
                user.getId(),
                "SUBSCRIPTION_ACTIVATED",
                "Your " + subscription.getPlanType() + " subscription has been activated.",
                subscription
        );

        // Send email to user
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            emailService.sendAccountStatusEmail(
                    user.getEmail(),
                    user.getUsername(),
                    "subscription_activated",
                    "Your " + subscription.getPlanType() + " subscription has been activated and will be valid until " + subscription.getEndDate().toString() + "."
            );
        }

        return new PaymentResponse(payment);
    }

    @Override
    public List<PaymentMethodResponse> getAllPaymentMethods() {
        List<PaymentMethod> paymentMethods = paymentMethodRepository.findAll();
        return paymentMethods.stream()
                .map(PaymentMethodResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentMethodResponse> getActivePaymentMethods() {
        List<PaymentMethod> paymentMethods = paymentMethodRepository.findByIsActive(true);
        return paymentMethods.stream()
                .map(PaymentMethodResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentMethodResponse getPaymentMethodById(Long paymentMethodId) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));
        return new PaymentMethodResponse(paymentMethod);
    }

    @Override
    public PaymentMethodResponse createPaymentMethod(PaymentMethod paymentMethod) {
        // Check if a payment method with the same name already exists
        if (paymentMethodRepository.findByName(paymentMethod.getName()).isPresent()) {
            throw new IllegalArgumentException("A payment method with this name already exists");
        }

        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(paymentMethod);
        return new PaymentMethodResponse(savedPaymentMethod);
    }

    @Override
    public PaymentMethodResponse updatePaymentMethod(Long paymentMethodId, PaymentMethod paymentMethod) {
        PaymentMethod existingPaymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        // Check if name is being changed and if a payment method with the new name already exists
        if (!existingPaymentMethod.getName().equals(paymentMethod.getName()) &&
                paymentMethodRepository.findByName(paymentMethod.getName()).isPresent()) {
            throw new IllegalArgumentException("A payment method with this name already exists");
        }

        existingPaymentMethod.setName(paymentMethod.getName());
        existingPaymentMethod.setDisplayName(paymentMethod.getDisplayName());
        existingPaymentMethod.setDescription(paymentMethod.getDescription());
        existingPaymentMethod.setIsActive(paymentMethod.getIsActive());
        existingPaymentMethod.setIconUrl(paymentMethod.getIconUrl());
        existingPaymentMethod.setProcessingFee(paymentMethod.getProcessingFee());
        existingPaymentMethod.setMinAmount(paymentMethod.getMinAmount());
        existingPaymentMethod.setMaxAmount(paymentMethod.getMaxAmount());

        PaymentMethod updatedPaymentMethod = paymentMethodRepository.save(existingPaymentMethod);
        return new PaymentMethodResponse(updatedPaymentMethod);
    }

    @Override
    public boolean deletePaymentMethod(Long paymentMethodId) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        // Check if the payment method is being used in any payments
        List<Payment> payments = paymentRepository.findByPaymentMethod(paymentMethod.getName());
        if (!payments.isEmpty()) {
            throw new IllegalStateException("Cannot delete payment method that is being used in payments");
        }

        paymentMethodRepository.delete(paymentMethod);
        return true;
    }
}
