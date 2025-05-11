package com.library.controller;

import com.library.model.User;
import com.library.payload.request.UpdateUserRoleRequest;
import com.library.payload.response.MessageResponse;
import com.library.payload.response.UserResponse;
import com.library.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = adminService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<MessageResponse> updateUser(
            @PathVariable Long id,
            @RequestBody User userDetails) {
        try {
            System.out.println("Received request to update user ID: " + id);
            adminService.updateUser(id, userDetails);
            return ResponseEntity.ok(new MessageResponse("User updated successfully"));
        } catch (Exception e) {
            System.err.println("Error updating user ID: " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse("Error updating user: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<MessageResponse> updateUserRole(
            @PathVariable Long id,
            @RequestBody UpdateUserRoleRequest updateUserRoleRequest) {
        try {
            adminService.updateUserRole(id, updateUserRoleRequest.getRoleName());
            return ResponseEntity.ok(new MessageResponse("User role updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error updating user role: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/roles")
    public ResponseEntity<MessageResponse> updateUserRoles(
            @PathVariable Long id,
            @RequestBody UpdateUserRoleRequest updateUserRoleRequest) {
        try {
            System.out.println("Received request to update roles for user ID: " + id);
            System.out.println("Roles to set: " + updateUserRoleRequest.getRoles());

            if (updateUserRoleRequest.getRoles() == null || updateUserRoleRequest.getRoles().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("No roles provided"));
            }

            adminService.updateUserRoles(id, updateUserRoleRequest.getRoles());
            System.out.println("Roles updated successfully for user ID: " + id);
            return ResponseEntity.ok(new MessageResponse("User roles updated successfully"));
        } catch (Exception e) {
            System.err.println("Error updating roles for user ID: " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse("Error updating user roles: " + e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
    }

    @PutMapping("/users/{id}/block")
    public ResponseEntity<MessageResponse> blockUser(@PathVariable Long id) {
        adminService.blockUser(id);
        return ResponseEntity.ok(new MessageResponse("User blocked successfully"));
    }

    @PutMapping("/users/{id}/unblock")
    public ResponseEntity<MessageResponse> unblockUser(@PathVariable Long id) {
        adminService.unblockUser(id);
        return ResponseEntity.ok(new MessageResponse("User unblocked successfully"));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    @GetMapping("/users/{id}/loans")
    public ResponseEntity<?> getUserLoans(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserLoans(id));
    }
}
