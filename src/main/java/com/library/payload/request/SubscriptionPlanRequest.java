package com.library.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public class SubscriptionPlanRequest {
    @NotBlank
    private String name;
    
    private String description;
    
    @NotNull
    @Positive
    private Double monthlyPrice;
    
    @Positive
    private Double annualPrice;
    
    @NotNull
    @Positive
    private Integer maxBooks;
    
    @NotNull
    @Positive
    private Integer loanDuration;
    
    @NotNull
    @PositiveOrZero
    private Integer gracePeriod;
    
    @NotNull
    @PositiveOrZero
    private Integer reservationPriority;
    
    @NotNull
    @PositiveOrZero
    private Double fineDiscount;
    
    private Boolean isActive = true;
    
    private List<String> features;

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
