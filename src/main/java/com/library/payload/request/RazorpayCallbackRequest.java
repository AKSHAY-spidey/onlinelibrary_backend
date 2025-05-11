package com.library.payload.request;

import jakarta.validation.constraints.NotBlank;

public class RazorpayCallbackRequest {
    @NotBlank
    private String razorpayPaymentId;
    
    @NotBlank
    private String razorpayOrderId;
    
    @NotBlank
    private String razorpaySignature;
    
    private String status;
    
    private String error;

    public RazorpayCallbackRequest() {
    }

    public RazorpayCallbackRequest(String razorpayPaymentId, String razorpayOrderId, String razorpaySignature, String status, String error) {
        this.razorpayPaymentId = razorpayPaymentId;
        this.razorpayOrderId = razorpayOrderId;
        this.razorpaySignature = razorpaySignature;
        this.status = status;
        this.error = error;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public String getRazorpaySignature() {
        return razorpaySignature;
    }

    public void setRazorpaySignature(String razorpaySignature) {
        this.razorpaySignature = razorpaySignature;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
