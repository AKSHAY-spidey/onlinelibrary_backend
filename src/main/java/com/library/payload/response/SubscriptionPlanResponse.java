package com.library.payload.response;

import com.library.model.SubscriptionPlan;
import java.util.List;

public class SubscriptionPlanResponse {
    private Long id;
    private String name;
    private String description;
    private Double monthlyPrice;
    private Double annualPrice;
    private Integer maxBooks;
    private Integer loanDuration;
    private Integer gracePeriod;
    private Integer reservationPriority;
    private Double fineDiscount;
    private Boolean isActive;
    private List<String> features;
    
    public SubscriptionPlanResponse() {
    }
    
    public SubscriptionPlanResponse(SubscriptionPlan plan) {
        this.id = plan.getId();
        this.name = plan.getName();
        this.description = plan.getDescription();
        this.monthlyPrice = plan.getMonthlyPrice();
        this.annualPrice = plan.getAnnualPrice();
        this.maxBooks = plan.getMaxBooks();
        this.loanDuration = plan.getLoanDuration();
        this.gracePeriod = plan.getGracePeriod();
        this.reservationPriority = plan.getReservationPriority();
        this.fineDiscount = plan.getFineDiscount();
        this.isActive = plan.getIsActive();
        
        // Parse features from JSON string if available
        if (plan.getFeatures() != null && !plan.getFeatures().isEmpty()) {
            try {
                // Simple parsing assuming format is a JSON array of strings
                String featuresStr = plan.getFeatures().replace("[", "").replace("]", "").replace("\"", "");
                this.features = List.of(featuresStr.split(","));
            } catch (Exception e) {
                this.features = List.of();
            }
        } else {
            this.features = List.of();
        }
    }

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

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }
}
