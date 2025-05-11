package com.library.repository;

import com.library.model.OtpType;
import com.library.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    
    Optional<OtpVerification> findByEmailAndOtpAndVerifiedFalseAndType(String email, String otp, OtpType type);
    
    Optional<OtpVerification> findTopByEmailAndTypeOrderByExpiryTimeDesc(String email, OtpType type);
    
    void deleteByEmailAndType(String email, OtpType type);
}
