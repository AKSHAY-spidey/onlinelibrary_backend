package com.library.payload.response;

import com.library.model.PaymentMethod;

public class PaymentMethodResponse {
    private Long id;
    private String name;
    private String displayName;
    private String description;
    private Boolean isActive;
    private String iconUrl;
    private Double processingFee;
    private Double minAmount;
    private Double maxAmount;
    
    public PaymentMethodResponse() {
    }
    
    public PaymentMethodResponse(PaymentMethod paymentMethod) {
        this.id = paymentMethod.getId();
        this.name = paymentMethod.getName();
        this.displayName = paymentMethod.getDisplayName();
        this.description = paymentMethod.getDescription();
        this.isActive = paymentMethod.getIsActive();
        this.iconUrl = paymentMethod.getIconUrl();
        this.processingFee = paymentMethod.getProcessingFee();
        this.minAmount = paymentMethod.getMinAmount();
        this.maxAmount = paymentMethod.getMaxAmount();
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Double getProcessingFee() {
        return processingFee;
    }

    public void setProcessingFee(Double processingFee) {
        this.processingFee = processingFee;
    }

    public Double getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Double minAmount) {
        this.minAmount = minAmount;
    }

    public Double getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Double maxAmount) {
        this.maxAmount = maxAmount;
    }
}
