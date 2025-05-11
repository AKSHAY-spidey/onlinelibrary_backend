package com.library.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verifications")
public class OtpVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String otp;
    
    @Column(nullable = false)
    private LocalDateTime expiryTime;
    
    @Column(nullable = false)
    private boolean verified;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpType type;
    
    // For storing user data during registration
    @Column(columnDefinition = "TEXT")
    private String userData;
    
    public OtpVerification() {
    }
    
    public OtpVerification(String email, String otp, LocalDateTime expiryTime, OtpType type) {
        this.email = email;
        this.otp = otp;
        this.expiryTime = expiryTime;
        this.verified = false;
        this.type = type;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getOtp() {
        return otp;
    }
    
    public void setOtp(String otp) {
        this.otp = otp;
    }
    
    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }
    
    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }
    
    public boolean isVerified() {
        return verified;
    }
    
    public void setVerified(boolean verified) {
        this.verified = verified;
    }
    
    public OtpType getType() {
        return type;
    }
    
    public void setType(OtpType type) {
        this.type = type;
    }
    
    public String getUserData() {
        return userData;
    }
    
    public void setUserData(String userData) {
        this.userData = userData;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryTime);
    }
}
