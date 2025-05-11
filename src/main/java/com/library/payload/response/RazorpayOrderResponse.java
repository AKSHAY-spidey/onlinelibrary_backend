package com.library.payload.response;

public class RazorpayOrderResponse {
    private String orderId;
    private String currency;
    private Integer amount;
    private String receipt;
    private String key;
    private String name;
    private String description;
    private String image;
    private String prefillName;
    private String prefillEmail;
    private String prefillContact;
    private String theme;

    public RazorpayOrderResponse() {
    }

    public RazorpayOrderResponse(String orderId, String currency, Integer amount, String receipt, String key, String name, String description, String image, String prefillName, String prefillEmail, String prefillContact, String theme) {
        this.orderId = orderId;
        this.currency = currency;
        this.amount = amount;
        this.receipt = receipt;
        this.key = key;
        this.name = name;
        this.description = description;
        this.image = image;
        this.prefillName = prefillName;
        this.prefillEmail = prefillEmail;
        this.prefillContact = prefillContact;
        this.theme = theme;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPrefillName() {
        return prefillName;
    }

    public void setPrefillName(String prefillName) {
        this.prefillName = prefillName;
    }

    public String getPrefillEmail() {
        return prefillEmail;
    }

    public void setPrefillEmail(String prefillEmail) {
        this.prefillEmail = prefillEmail;
    }

    public String getPrefillContact() {
        return prefillContact;
    }

    public void setPrefillContact(String prefillContact) {
        this.prefillContact = prefillContact;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
