package com.library.controller;

import com.library.model.PaymentMethod;
import com.library.payload.response.MessageResponse;
import com.library.payload.response.PaymentMethodResponse;
import com.library.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<PaymentMethodResponse>> getAllPaymentMethods() {
        List<PaymentMethodResponse> paymentMethods = paymentService.getAllPaymentMethods();
        return ResponseEntity.ok(paymentMethods);
    }

    @GetMapping("/active")
    public ResponseEntity<List<PaymentMethodResponse>> getActivePaymentMethods() {
        List<PaymentMethodResponse> paymentMethods = paymentService.getActivePaymentMethods();
        return ResponseEntity.ok(paymentMethods);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethodResponse> getPaymentMethodById(@PathVariable("id") Long id) {
        PaymentMethodResponse paymentMethod = paymentService.getPaymentMethodById(id);
        return ResponseEntity.ok(paymentMethod);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentMethodResponse> createPaymentMethod(@Valid @RequestBody PaymentMethod paymentMethod) {
        PaymentMethodResponse createdPaymentMethod = paymentService.createPaymentMethod(paymentMethod);
        return ResponseEntity.ok(createdPaymentMethod);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentMethodResponse> updatePaymentMethod(
            @PathVariable("id") Long id,
            @Valid @RequestBody PaymentMethod paymentMethod) {
        PaymentMethodResponse updatedPaymentMethod = paymentService.updatePaymentMethod(id, paymentMethod);
        return ResponseEntity.ok(updatedPaymentMethod);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deletePaymentMethod(@PathVariable("id") Long id) {
        boolean deleted = paymentService.deletePaymentMethod(id);
        if (deleted) {
            return ResponseEntity.ok(new MessageResponse("Payment method deleted successfully"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Failed to delete payment method"));
        }
    }
}
