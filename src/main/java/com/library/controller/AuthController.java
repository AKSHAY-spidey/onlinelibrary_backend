package com.library.controller;

import com.library.exception.TokenRefreshException;
import com.library.model.ERole;
import com.library.model.RefreshToken;
import com.library.model.Role;
import com.library.model.User;
import com.library.payload.request.*;
import com.library.payload.response.JwtResponse;
import com.library.exception.UserBlockedException;
import com.library.payload.response.MessageResponse;
import com.library.payload.response.TokenRefreshResponse;
import com.library.repository.RoleRepository;
import com.library.repository.UserRepository;
import com.library.security.jwt.JwtUtils;
import com.library.security.services.UserDetailsImpl;
import com.library.security.services.UserDetailsServiceImpl;
import com.library.service.BrevoEmailService;
import com.library.service.OtpService;
import com.library.service.RefreshTokenService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    OtpService otpService;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    BrevoEmailService emailService;

    @Autowired
    RefreshTokenService refreshTokenService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // First check if the user is blocked before authentication
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            if (!userDetails.isEnabled()) {
                return ResponseEntity
                        .status(HttpServletResponse.SC_FORBIDDEN)
                        .body(new MessageResponse("Your account has been blocked. Please contact the administrator."));
            }

            // Proceed with authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl authenticatedUser = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = authenticatedUser.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            // Create a refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(authenticatedUser.getId());

            return ResponseEntity.ok(new JwtResponse(jwt,
                    refreshToken.getToken(),
                    authenticatedUser.getId(),
                    authenticatedUser.getUsername(),
                    authenticatedUser.getEmail(),
                    roles));
        } catch (UserBlockedException e) {
            return ResponseEntity
                    .status(HttpServletResponse.SC_FORBIDDEN)
                    .body(new MessageResponse("Your account has been blocked. Please contact the administrator."));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: User not found!"));
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Invalid username or password!"));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/signup/request")
    public ResponseEntity<?> requestRegistration(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Generate and send OTP for registration
        otpService.generateAndSendRegistrationOtp(signUpRequest);

        return ResponseEntity.ok(new MessageResponse("OTP sent to your email. Please verify to complete registration."));
    }

    @PostMapping("/signup/verify")
    public ResponseEntity<?> verifyAndRegister(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest) {
        // Verify OTP and get the stored signup request
        return otpService.verifyRegistrationOtp(verifyOtpRequest.getEmail(), verifyOtpRequest.getOtp())
                .map(signUpRequest -> {
                    // Create new user's account
                    User user = new User(signUpRequest.getUsername(),
                            signUpRequest.getEmail(),
                            encoder.encode(signUpRequest.getPassword()));

                    user.setFirstName(signUpRequest.getFirstName());
                    user.setLastName(signUpRequest.getLastName());
                    user.setPhoneNumber(signUpRequest.getPhoneNumber());
                    user.setAddress(signUpRequest.getAddress());

                    Set<String> strRoles = signUpRequest.getRole();
                    Set<Role> roles = new HashSet<>();

                    if (strRoles == null) {
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                    } else {
                        strRoles.forEach(role -> {
                            switch (role) {
                                case "admin":
                                    Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                                    roles.add(adminRole);
                                    break;
                                case "librarian":
                                    Role librarianRole = roleRepository.findByName(ERole.ROLE_LIBRARIAN)
                                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                                    roles.add(librarianRole);
                                    break;
                                default:
                                    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                                    roles.add(userRole);
                            }
                        });
                    }

                    user.setRoles(roles);
                    User savedUser = userRepository.save(user);

                    // Send welcome email to the newly registered user
                    emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getUsername());

                    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
                })
                .orElse(ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Invalid or expired OTP!")));
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        String email = forgotPasswordRequest.getEmail();

        // Check if user exists with this email
        if (!userRepository.existsByEmail(email)) {
            // For security reasons, don't reveal that the email doesn't exist
            // Just return a generic message
            return ResponseEntity.ok(new MessageResponse("If your email is registered, you will receive a password reset OTP."));
        }

        // Generate and send OTP for password reset
        otpService.generateAndSendPasswordResetOtp(email);

        return ResponseEntity.ok(new MessageResponse("Password reset OTP sent to your email."));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        String email = resetPasswordRequest.getEmail();
        String otp = resetPasswordRequest.getOtp();
        String newPassword = resetPasswordRequest.getNewPassword();

        // Verify OTP for password reset
        boolean isOtpValid = otpService.verifyPasswordResetOtp(email, otp);

        if (!isOtpValid) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Invalid or expired OTP!"));
        }

        // Find user by email
        return userRepository.findByEmail(email)
                .map(user -> {
                    // Update password
                    user.setPassword(encoder.encode(newPassword));
                    userRepository.save(user);

                    return ResponseEntity.ok(new MessageResponse("Password has been reset successfully!"));
                })
                .orElse(ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: User not found!")));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();
        refreshTokenService.deleteByUserId(userId);

        // Clear the security context
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("Log out successful!"));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateJwtToken(user.getUsername());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }
}
