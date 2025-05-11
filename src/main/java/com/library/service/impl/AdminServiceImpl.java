package com.library.service.impl;

import com.library.exception.ResourceNotFoundException;
import com.library.model.ERole;
import com.library.model.Role;
import com.library.model.User;
import com.library.payload.response.UserResponse;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.RoleRepository;
import com.library.repository.UserRepository;
import com.library.service.AdminService;
import com.library.service.BrevoEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BrevoEmailService emailService;

    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return new UserResponse(user);
    }

    @Override
    @Transactional
    public void updateUser(Long id, User userDetails) {
        System.out.println("AdminServiceImpl.updateUser - userId: " + id + ", userDetails: " + userDetails);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Update user fields if provided
        if (userDetails.getUsername() != null) {
            user.setUsername(userDetails.getUsername());
        }

        if (userDetails.getEmail() != null) {
            user.setEmail(userDetails.getEmail());
        }

        if (userDetails.getFirstName() != null) {
            user.setFirstName(userDetails.getFirstName());
        }

        if (userDetails.getLastName() != null) {
            user.setLastName(userDetails.getLastName());
        }

        if (userDetails.getAddress() != null) {
            user.setAddress(userDetails.getAddress());
        }

        if (userDetails.getPhoneNumber() != null) {
            user.setPhoneNumber(userDetails.getPhoneNumber());
        }

        // Don't update password here - that should be done through a separate endpoint with proper validation

        User savedUser = userRepository.save(user);
        System.out.println("User updated successfully: " + savedUser.getId());

        // Send email notification to the user about the update
        if (savedUser.getEmail() != null && !savedUser.getEmail().isEmpty()) {
            try {

                emailService.sendAccountStatusEmail(
                    savedUser.getEmail(),
                    savedUser.getUsername(),
                    "updated",
                    "Your account information has been updated by an administrator."
                );

                System.out.println("Account update email sent to: " + savedUser.getEmail());
            } catch (Exception e) {
                System.err.println("Failed to send account update email: " + e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void updateUserRole(Long userId, String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name must be provided");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        ERole eRole;
        try {
            eRole = ERole.valueOf(roleName.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role name: " + roleName);
        }

        Role role = roleRepository.findByName(eRole)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        // Clear existing roles and add the new one
        Set<Role> newRoles = new HashSet<>();
        newRoles.add(role);
        user.setRoles(newRoles);

        User savedUser = userRepository.save(user);

        // Send email notification to the user about the role update
        if (savedUser.getEmail() != null && !savedUser.getEmail().isEmpty()) {
            try {

                emailService.sendAccountStatusEmail(
                    savedUser.getEmail(),
                    savedUser.getUsername(),
                    "role_updated",
                    "Your account role has been updated to " + roleName + "."
                );

                System.out.println("Role update email sent to: " + savedUser.getEmail());
            } catch (Exception e) {
                System.err.println("Failed to send role update email: " + e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void updateUserRoles(Long userId, List<String> roleNames) {
        System.out.println("AdminServiceImpl.updateUserRoles - userId: " + userId + ", roleNames: " + roleNames);

        if (roleNames == null || roleNames.isEmpty()) {
            throw new IllegalArgumentException("At least one role must be provided");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        System.out.println("Found user: " + user.getUsername() + ", current roles: " + user.getRoles());

        // Clear existing roles
        user.getRoles().clear();
        System.out.println("Cleared existing roles");

        // Add each role
        Set<Role> newRoles = new HashSet<>();
        for (String roleName : roleNames) {
            if (roleName == null || roleName.trim().isEmpty()) {
                System.out.println("Skipping empty role name");
                continue; // Skip empty role names
            }

            System.out.println("Processing role: " + roleName);
            ERole eRole;
            try {
                eRole = ERole.valueOf(roleName.trim());
                System.out.println("Parsed ERole: " + eRole);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid role name: " + roleName);
                throw new IllegalArgumentException("Invalid role name: " + roleName);
            }

            Role role = roleRepository.findByName(eRole)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

            System.out.println("Found role in database: " + role.getName());
            newRoles.add(role);
            System.out.println("Added role to new roles set");
        }

        // Ensure user has at least the USER role
        if (newRoles.isEmpty()) {
            System.out.println("No roles were added, adding default ROLE_USER");
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new ResourceNotFoundException("Default role not found: ROLE_USER"));
            newRoles.add(userRole);
        }

        System.out.println("Setting new roles: " + newRoles);
        user.setRoles(newRoles);

        User savedUser = userRepository.save(user);
        System.out.println("User saved with ID: " + savedUser.getId() + ", roles: " + savedUser.getRoles());

        // Send email notification to the user about the roles update
        if (savedUser.getEmail() != null && !savedUser.getEmail().isEmpty()) {
            try {
                // Create a comma-separated list of role names
                String rolesList = savedUser.getRoles().stream()
                    .map(role -> role.getName().name().replace("ROLE_", ""))
                    .collect(Collectors.joining(", "));


                emailService.sendAccountStatusEmail(
                    savedUser.getEmail(),
                    savedUser.getUsername(),
                    "roles_updated",
                    "Your account roles have been updated to: " + rolesList + "."
                );

                System.out.println("Roles update email sent to: " + savedUser.getEmail());
            } catch (Exception e) {
                System.err.println("Failed to send roles update email: " + e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Send account deletion email before deleting the user
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            emailService.sendAccountStatusEmail(
                user.getEmail(),
                user.getUsername(),
                "deleted",
                "Your account has been deleted by an administrator."
            );
        }

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Handle null values for active field
        if (user.isActive() == null || user.isActive()) {
            user.setActive(false);
            userRepository.save(user);

            // Send account blocked email notification
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                emailService.sendAccountStatusEmail(
                    user.getEmail(),
                    user.getUsername(),
                    "blocked",
                    "Your account has been temporarily blocked due to a violation of our terms of service or at an administrator's discretion."
                );
            }
        }
    }

    @Override
    @Transactional
    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Handle null values for active field
        if (user.isActive() == null || !user.isActive()) {
            user.setActive(true);
            userRepository.save(user);

            // Send account unblocked email notification
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                emailService.sendAccountStatusEmail(
                    user.getEmail(),
                    user.getUsername(),
                    "unblocked",
                    "Your account has been unblocked and is now active again. You can now log in and use all library services."
                );
            }
        }
    }

    @Override
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        // User statistics
        long totalUsers = userRepository.count();
        long adminCount = userRepository.countByRolesName(ERole.ROLE_ADMIN);
        long librarianCount = userRepository.countByRolesName(ERole.ROLE_LIBRARIAN);
        long regularUserCount = userRepository.countByRolesName(ERole.ROLE_USER);

        // Book statistics
        long totalBooks = bookRepository.count();

        // Loan statistics
        long totalLoans = loanRepository.count();

        stats.put("totalUsers", totalUsers);
        stats.put("adminCount", adminCount);
        stats.put("librarianCount", librarianCount);
        stats.put("regularUserCount", regularUserCount);
        stats.put("totalBooks", totalBooks);
        stats.put("totalLoans", totalLoans);

        return stats;
    }

    @Override
    public List<?> getUserLoans(Long userId) {
        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Get all loans for the user
        return loanRepository.findByUserId(userId);
    }
}
