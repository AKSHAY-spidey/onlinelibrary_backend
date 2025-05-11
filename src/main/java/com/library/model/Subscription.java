package com.library.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "plan_type", nullable = false)
    private String planType; // BASIC, STANDARD, PREMIUM

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "auto_renew")
    private Boolean autoRenew = false;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "signature")
    private String signature;

    @Column(name = "status")
    private String status; // ACTIVE, EXPIRED, CANCELLED

    @Column(name = "price")
    private Double price;

    // Benefits
    @Column(name = "max_books")
    private Integer maxBooks; // Maximum number of books that can be borrowed at once

    @Column(name = "loan_duration")
    private Integer loanDuration; // Loan duration in days

    @Column(name = "grace_period")
    private Integer gracePeriod; // Grace period for returns in days

    @Column(name = "reservation_priority")
    private Integer reservationPriority; // Priority for reservations (higher = better)

    @Column(name = "fine_discount")
    private Double fineDiscount; // Percentage discount on fines

    public Subscription() {
    }

    public Subscription(User user, String planType, LocalDateTime startDate, LocalDateTime endDate,
                        Boolean autoRenew, String paymentId, String orderId, String signature, String status, Double price,
                        Integer maxBooks, Integer loanDuration, Integer gracePeriod,
                        Integer reservationPriority, Double fineDiscount) {
        this.user = user;
        this.planType = planType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.autoRenew = autoRenew;
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.signature = signature;
        this.status = status;
        this.price = price;
        this.maxBooks = maxBooks;
        this.loanDuration = loanDuration;
        this.gracePeriod = gracePeriod;
        this.reservationPriority = reservationPriority;
        this.fineDiscount = fineDiscount;
    }

    // Constructor without order details for backward compatibility
    public Subscription(User user, String planType, LocalDateTime startDate, LocalDateTime endDate,
                        Boolean autoRenew, String paymentId, String status, Double price,
                        Integer maxBooks, Integer loanDuration, Integer gracePeriod,
                        Integer reservationPriority, Double fineDiscount) {
        this(user, planType, startDate, endDate, autoRenew, paymentId, null, null, status, price,
             maxBooks, loanDuration, gracePeriod, reservationPriority, fineDiscount);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Boolean getAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(Boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getMaxBooks() {
        return maxBooks;
    }

    public void setMaxBooks(Integer maxBooks) {
        this.maxBooks = maxBooks;
    }

    public Integer getLoanDuration() {
        return loanDuration;
    }

    public void setLoanDuration(Integer loanDuration) {
        this.loanDuration = loanDuration;
    }

    public Integer getGracePeriod() {
        return gracePeriod;
    }

    public void setGracePeriod(Integer gracePeriod) {
        this.gracePeriod = gracePeriod;
    }

    public Integer getReservationPriority() {
        return reservationPriority;
    }

    public void setReservationPriority(Integer reservationPriority) {
        this.reservationPriority = reservationPriority;
    }

    public Double getFineDiscount() {
        return fineDiscount;
    }

    public void setFineDiscount(Double fineDiscount) {
        this.fineDiscount = fineDiscount;
    }

    // Helper methods
    public boolean isActive() {
        return "ACTIVE".equals(status) &&
               LocalDateTime.now().isAfter(startDate) &&
               LocalDateTime.now().isBefore(endDate);
    }
}
