package com.library.payload.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class UpdateUserRoleRequest {
    private String roleName;

    private List<String> roles;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
