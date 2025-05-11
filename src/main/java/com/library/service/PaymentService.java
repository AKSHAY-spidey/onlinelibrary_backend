package com.library.service;

import com.library.model.Payment;
import com.library.model.PaymentMethod;
import com.library.payload.request.PaymentRequest;
import com.library.payload.request.RazorpayCallbackRequest;
import com.library.payload.request.SubscriptionRequest;
import com.library.payload.response.PaymentMethodResponse;
import com.library.payload.response.PaymentResponse;
import com.library.payload.response.RazorpayOrderResponse;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {
    /**
     * Create a payment order with Razorpay
     *
     * @param authentication The authenticated user
     * @param loanId The ID of the loan to pay for
     * @return The Razorpay order response
     */
    RazorpayOrderResponse createRazorpayOrder(Authentication authentication, Long loanId);

    /**
     * Create a fine payment order with Razorpay
     *
     * @param authentication The authenticated user
     * @param loanId The ID of the loan with fine to pay for
     * @return The Razorpay order response
     */
    RazorpayOrderResponse createFinePaymentOrder(Authentication authentication, Long loanId);

    /**
     * Process a Razorpay payment callback
     *
     * @param authentication The authenticated user
     * @param callbackRequest The Razorpay callback request
     * @return The payment response
     */
    PaymentResponse processRazorpayCallback(Authentication authentication, RazorpayCallbackRequest callbackRequest);

    /**
     * Create a manual payment
     *
     * @param authentication The authenticated user
     * @param paymentRequest The payment request
     * @return The payment response
     */
    PaymentResponse createManualPayment(Authentication authentication, PaymentRequest paymentRequest);

    /**
     * Verify a manual payment
     *
     * @param authentication The authenticated user (admin or librarian)
     * @param paymentId The ID of the payment to verify
     * @return The payment response
     */
    PaymentResponse verifyManualPayment(Authentication authentication, Long paymentId);

    /**
     * Get all payments for the authenticated user
     *
     * @param authentication The authenticated user
     * @return List of payment responses
     */
    List<PaymentResponse> getUserPayments(Authentication authentication);

    /**
     * Get all payments (admin or librarian only)
     *
     * @return List of payment responses
     */
    List<PaymentResponse> getAllPayments();

    /**
     * Get payments by status (admin or librarian only)
     *
     * @param status The payment status
     * @return List of payment responses
     */
    List<PaymentResponse> getPaymentsByStatus(String status);

    /**
     * Get payments by date range (admin or librarian only)
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return List of payment responses
     */
    List<PaymentResponse> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get unverified manual payments (admin or librarian only)
     *
     * @return List of payment responses
     */
    List<PaymentResponse> getUnverifiedManualPayments();

    /**
     * Get payment by ID
     *
     * @param authentication The authenticated user
     * @param paymentId The ID of the payment
     * @return The payment response
     */
    PaymentResponse getPaymentById(Authentication authentication, Long paymentId);

    /**
     * Generate a payment receipt PDF
     *
     * @param authentication The authenticated user
     * @param paymentId The ID of the payment
     * @return The PDF file as byte array
     */
    byte[] generatePaymentReceipt(Authentication authentication, Long paymentId);

    /**
     * Create a payment order for a subscription
     *
     * @param authentication The authenticated user
     * @param subscriptionRequest The subscription request
     * @return The Razorpay order response
     */
    RazorpayOrderResponse createSubscriptionOrder(Authentication authentication, SubscriptionRequest subscriptionRequest);

    /**
     * Create a payment order for changing a subscription plan
     *
     * @param authentication The authenticated user
     * @param subscriptionRequest The subscription request containing the new plan details
     * @return The Razorpay order response
     */
    RazorpayOrderResponse createSubscriptionChangeOrder(Authentication authentication, SubscriptionRequest subscriptionRequest);

    /**
     * Process a subscription payment
     *
     * @param authentication The authenticated user
     * @param subscriptionId The ID of the subscription
     * @param paymentId The ID of the payment
     * @return The payment response
     */
    PaymentResponse processSubscriptionPayment(Authentication authentication, Long subscriptionId, Long paymentId);

    /**
     * Get all payment methods
     *
     * @return List of payment method responses
     */
    List<PaymentMethodResponse> getAllPaymentMethods();

    /**
     * Get active payment methods
     *
     * @return List of active payment method responses
     */
    List<PaymentMethodResponse> getActivePaymentMethods();

    /**
     * Get payment method by ID
     *
     * @param paymentMethodId The ID of the payment method
     * @return The payment method response
     */
    PaymentMethodResponse getPaymentMethodById(Long paymentMethodId);

    /**
     * Create a payment method (admin only)
     *
     * @param paymentMethod The payment method to create
     * @return The created payment method response
     */
    PaymentMethodResponse createPaymentMethod(PaymentMethod paymentMethod);

    /**
     * Update a payment method (admin only)
     *
     * @param paymentMethodId The ID of the payment method to update
     * @param paymentMethod The updated payment method
     * @return The updated payment method response
     */
    PaymentMethodResponse updatePaymentMethod(Long paymentMethodId, PaymentMethod paymentMethod);

    /**
     * Delete a payment method (admin only)
     *
     * @param paymentMethodId The ID of the payment method to delete
     * @return True if deleted successfully
     */
    boolean deletePaymentMethod(Long paymentMethodId);
}
