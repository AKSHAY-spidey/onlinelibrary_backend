package com.library.model;

import jakarta.persistence.*;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "monthly_price", nullable = false)
    private Double monthlyPrice;
    
    @Column(name = "annual_price")
    private Double annualPrice;
    
    @Column(name = "max_books", nullable = false)
    private Integer maxBooks;
    
    @Column(name = "loan_duration", nullable = false)
    private Integer loanDuration;
    
    @Column(name = "grace_period", nullable = false)
    private Integer gracePeriod;
    
    @Column(name = "reservation_priority", nullable = false)
    private Integer reservationPriority;
    
    @Column(name = "fine_discount", nullable = false)
    private Double fineDiscount;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "features")
    private String features; // JSON string of additional features
    
    public SubscriptionPlan() {
    }
    
    public SubscriptionPlan(String name, String description, Double monthlyPrice, Double annualPrice,
                           Integer maxBooks, Integer loanDuration, Integer gracePeriod,
                           Integer reservationPriority, Double fineDiscount, Boolean isActive,
                           String features) {
        this.name = name;
        this.description = description;
        this.monthlyPrice = monthlyPrice;
        this.annualPrice = annualPrice;
        this.maxBooks = maxBooks;
        this.loanDuration = loanDuration;
        this.gracePeriod = gracePeriod;
        this.reservationPriority = reservationPriority;
        this.fineDiscount = fineDiscount;
        this.isActive = isActive;
        this.features = features;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(Double monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public Double getAnnualPrice() {
        return annualPrice;
    }

    public void setAnnualPrice(Double annualPrice) {
        this.annualPrice = annualPrice;
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

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }
}
