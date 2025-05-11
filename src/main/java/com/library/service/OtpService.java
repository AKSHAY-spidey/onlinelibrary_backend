package com.library.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.model.OtpType;
import com.library.model.OtpVerification;
import com.library.payload.request.SignupRequest;
import com.library.repository.OtpVerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;

    @Autowired
    private OtpVerificationRepository otpVerificationRepository;

    @Autowired
    private BrevoEmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Generate and send OTP for registration
     */
    @Transactional
    public void generateAndSendRegistrationOtp(SignupRequest signupRequest) {
        try {
            String email = signupRequest.getEmail();

            // Delete any existing OTPs for this email and type
            otpVerificationRepository.deleteByEmailAndType(email, OtpType.REGISTRATION);

            // Generate new OTP
            String otp = generateOtp();
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

            // Create OTP verification record
            OtpVerification otpVerification = new OtpVerification(email, otp, expiryTime, OtpType.REGISTRATION);

            // Store user data as JSON
            otpVerification.setUserData(objectMapper.writeValueAsString(signupRequest));

            // Save to database
            otpVerificationRepository.save(otpVerification);

            // Send OTP via email
            emailService.sendOtpEmail(email, otp, "Account Registration");

            logger.info("Registration OTP sent to: {}", email);
        } catch (Exception e) {
            logger.error("Error generating registration OTP", e);
            throw new RuntimeException("Failed to generate registration OTP", e);
        }
    }

    /**
     * Generate and send OTP for password reset
     */
    @Transactional
    public void generateAndSendPasswordResetOtp(String email) {
        try {
            // Delete any existing OTPs for this email and type
            otpVerificationRepository.deleteByEmailAndType(email, OtpType.PASSWORD_RESET);

            // Generate new OTP
            String otp = generateOtp();
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

            // Create OTP verification record
            OtpVerification otpVerification = new OtpVerification(email, otp, expiryTime, OtpType.PASSWORD_RESET);

            // Save to database
            otpVerificationRepository.save(otpVerification);

            // Send OTP via email
            emailService.sendOtpEmail(email, otp, "Password Reset");

            logger.info("Password reset OTP sent to: {}", email);
        } catch (Exception e) {
            logger.error("Error generating password reset OTP", e);
            throw new RuntimeException("Failed to generate password reset OTP", e);
        }
    }

    /**
     * Verify OTP for registration
     */
    @Transactional
    public Optional<SignupRequest> verifyRegistrationOtp(String email, String otp) {
        try {
            Optional<OtpVerification> otpVerificationOpt = otpVerificationRepository
                    .findByEmailAndOtpAndVerifiedFalseAndType(email, otp, OtpType.REGISTRATION);

            if (otpVerificationOpt.isPresent()) {
                OtpVerification otpVerification = otpVerificationOpt.get();

                if (otpVerification.isExpired()) {
                    logger.info("OTP expired for email: {}", email);
                    return Optional.empty();
                }

                // Mark as verified
                otpVerification.setVerified(true);
                otpVerificationRepository.save(otpVerification);

                // Convert stored user data back to SignupRequest
                SignupRequest signupRequest = objectMapper.readValue(
                        otpVerification.getUserData(), SignupRequest.class);

                return Optional.of(signupRequest);
            }

            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error verifying registration OTP", e);
            throw new RuntimeException("Failed to verify registration OTP", e);
        }
    }

    /**
     * Verify OTP for password reset
     */
    @Transactional
    public boolean verifyPasswordResetOtp(String email, String otp) {
        Optional<OtpVerification> otpVerificationOpt = otpVerificationRepository
                .findByEmailAndOtpAndVerifiedFalseAndType(email, otp, OtpType.PASSWORD_RESET);

        if (otpVerificationOpt.isPresent()) {
            OtpVerification otpVerification = otpVerificationOpt.get();

            if (otpVerification.isExpired()) {
                logger.info("Password reset OTP expired for email: {}", email);
                return false;
            }

            // Mark as verified
            otpVerification.setVerified(true);
            otpVerificationRepository.save(otpVerification);

            return true;
        }

        return false;
    }

    /**
     * Generate a random OTP
     */
    private String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }

        return otp.toString();
    }
}
