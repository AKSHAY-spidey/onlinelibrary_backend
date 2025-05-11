package com.library.payload.response;

import com.library.model.Subscription;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SubscriptionResponse {
    private Long id;
    private UserResponse user;
    private String planType;
    private String startDate;
    private String endDate;
    private Boolean autoRenew;
    private String paymentId;
    private String orderId;
    private String signature;
    private String status;
    private Double price;
    private Integer maxBooks;
    private Integer loanDuration;
    private Integer gracePeriod;
    private Integer reservationPriority;
    private Double fineDiscount;
    private Boolean isActive;
    private Long daysRemaining;

    public SubscriptionResponse() {
    }

    public SubscriptionResponse(Subscription subscription) {
        this.id = subscription.getId();
        this.user = new UserResponse(subscription.getUser());
        this.planType = subscription.getPlanType();

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        this.startDate = subscription.getStartDate().format(formatter);
        this.endDate = subscription.getEndDate().format(formatter);

        this.autoRenew = subscription.getAutoRenew();
        this.paymentId = subscription.getPaymentId();
        this.orderId = subscription.getOrderId();
        this.signature = subscription.getSignature();
        this.status = subscription.getStatus();
        this.price = subscription.getPrice();
        this.maxBooks = subscription.getMaxBooks();
        this.loanDuration = subscription.getLoanDuration();
        this.gracePeriod = subscription.getGracePeriod();
        this.reservationPriority = subscription.getReservationPriority();
        this.fineDiscount = subscription.getFineDiscount();
        this.isActive = subscription.isActive();

        // Calculate days remaining
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(subscription.getEndDate())) {
            this.daysRemaining = java.time.Duration.between(now, subscription.getEndDate()).toDays();
        } else {
            this.daysRemaining = 0L;
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

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(Long daysRemaining) {
        this.daysRemaining = daysRemaining;
    }
}
