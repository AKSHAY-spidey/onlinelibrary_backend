package com.library.service;

import com.library.model.User;
import com.library.payload.response.UserResponse;

import java.util.List;
import java.util.Map;

public interface AdminService {
    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    void updateUser(Long id, User userDetails);

    void updateUserRole(Long userId, String roleName);

    void updateUserRoles(Long userId, List<String> roleNames);

    void deleteUser(Long id);

    void blockUser(Long userId);

    void unblockUser(Long userId);

    Map<String, Object> getSystemStats();

    List<?> getUserLoans(Long userId);
}
