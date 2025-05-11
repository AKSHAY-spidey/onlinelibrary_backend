package com.library.controller;

import com.library.payload.request.PaymentRequest;
import com.library.payload.request.RazorpayCallbackRequest;
import com.library.payload.request.SubscriptionRequest;
import com.library.payload.response.MessageResponse;
import com.library.payload.response.PaymentResponse;
import com.library.payload.response.RazorpayOrderResponse;
import com.library.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/razorpay/create/{loanId}")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<RazorpayOrderResponse> createRazorpayOrder(
            Authentication authentication,
            @PathVariable Long loanId) {
        RazorpayOrderResponse response = paymentService.createRazorpayOrder(authentication, loanId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/razorpay/fine/{loanId}")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<RazorpayOrderResponse> createFinePaymentOrder(
            Authentication authentication,
            @PathVariable Long loanId) {
        RazorpayOrderResponse response = paymentService.createFinePaymentOrder(authentication, loanId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/razorpay/callback")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> processRazorpayCallback(
            Authentication authentication,
            @Valid @RequestBody RazorpayCallbackRequest callbackRequest) {
        PaymentResponse response = paymentService.processRazorpayCallback(authentication, callbackRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/razorpay/verify")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> verifyRazorpayPayment(
            Authentication authentication,
            @Valid @RequestBody RazorpayCallbackRequest callbackRequest) {
        PaymentResponse response = paymentService.processRazorpayCallback(authentication, callbackRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/manual")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> createManualPayment(
            Authentication authentication,
            @Valid @RequestBody PaymentRequest paymentRequest) {
        PaymentResponse response = paymentService.createManualPayment(authentication, paymentRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/manual/verify/{paymentId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> verifyManualPayment(
            Authentication authentication,
            @PathVariable Long paymentId) {
        PaymentResponse response = paymentService.verifyManualPayment(authentication, paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getUserPayments(Authentication authentication) {
        List<PaymentResponse> payments = paymentService.getUserPayments(authentication);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<PaymentResponse> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(@PathVariable String status) {
        List<PaymentResponse> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<PaymentResponse> payments = paymentService.getPaymentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/unverified")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getUnverifiedManualPayments() {
        List<PaymentResponse> payments = paymentService.getUnverifiedManualPayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> getPaymentById(
            Authentication authentication,
            @PathVariable Long paymentId) {
        PaymentResponse payment = paymentService.getPaymentById(authentication, paymentId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/{paymentId}/receipt")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> generatePaymentReceipt(
            Authentication authentication,
            @PathVariable Long paymentId) {
        byte[] receipt = paymentService.generatePaymentReceipt(authentication, paymentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "payment_receipt_" + paymentId + ".pdf");

        return new ResponseEntity<>(receipt, headers, HttpStatus.OK);
    }

    @PostMapping("/subscription/order")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<RazorpayOrderResponse> createSubscriptionOrder(
            Authentication authentication,
            @Valid @RequestBody SubscriptionRequest subscriptionRequest) {
        RazorpayOrderResponse response = paymentService.createSubscriptionOrder(authentication, subscriptionRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/subscription/change-order")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<RazorpayOrderResponse> createSubscriptionChangeOrder(
            Authentication authentication,
            @Valid @RequestBody SubscriptionRequest subscriptionRequest) {
        RazorpayOrderResponse response = paymentService.createSubscriptionChangeOrder(authentication, subscriptionRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/subscription/{subscriptionId}/payment/{paymentId}")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> processSubscriptionPayment(
            Authentication authentication,
            @PathVariable Long subscriptionId,
            @PathVariable Long paymentId) {
        PaymentResponse response = paymentService.processSubscriptionPayment(authentication, subscriptionId, paymentId);
        return ResponseEntity.ok(response);
    }
}
