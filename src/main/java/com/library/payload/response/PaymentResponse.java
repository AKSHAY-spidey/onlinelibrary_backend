package com.library.payload.response;

import com.library.model.Payment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentResponse {
    private Long id;
    private UserResponse user;
    private LoanResponse loan;
    private Double amount;
    private String paymentMethod;
    private String transactionId;
    private String receiptUrl;
    private String paymentDate;
    private String status;
    private String description;
    private String receiptNumber;
    private Boolean verified;
    private String verifiedBy;
    private String verificationDate;

    public PaymentResponse() {
    }

    public PaymentResponse(Payment payment) {
        this.id = payment.getId();
        this.user = new UserResponse(payment.getUser());
        
        if (payment.getLoan() != null) {
            this.loan = new LoanResponse(payment.getLoan());
        }
        
        this.amount = payment.getAmount();
        this.paymentMethod = payment.getPaymentMethod();
        this.transactionId = payment.getTransactionId();
        this.receiptUrl = payment.getReceiptUrl();
        
        if (payment.getPaymentDate() != null) {
            this.paymentDate = payment.getPaymentDate().format(DateTimeFormatter.ISO_DATE_TIME);
        }
        
        this.status = payment.getStatus();
        this.description = payment.getDescription();
        this.receiptNumber = payment.getReceiptNumber();
        this.verified = payment.getVerified();
        this.verifiedBy = payment.getVerifiedBy();
        
        if (payment.getVerificationDate() != null) {
            this.verificationDate = payment.getVerificationDate().format(DateTimeFormatter.ISO_DATE_TIME);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public LoanResponse getLoan() {
        return loan;
    }

    public void setLoan(LoanResponse loan) {
        this.loan = loan;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getReceiptUrl() {
        return receiptUrl;
    }

    public void setReceiptUrl(String receiptUrl) {
        this.receiptUrl = receiptUrl;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public String getVerificationDate() {
        return verificationDate;
    }

    public void setVerificationDate(String verificationDate) {
        this.verificationDate = verificationDate;
    }
}
