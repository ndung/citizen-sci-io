package io.sci.citizen.model.dto;

import io.sci.citizen.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class UserRequest {

    private Long id;
    @NotBlank @Size(max = 64)
    private String username;

    @NotBlank @Size(max = 120)
    private String fullName;

    @NotBlank @Size(max = 160) @Email
    private String email;

    private String password;

    private String confirmPassword;

    private boolean enabled = true;

    // CSV roles, e.g.: "ADMIN, USER"
    private String rolesCsv;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // getters/setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getRolesCsv() { return rolesCsv; }
    public void setRolesCsv(String rolesCsv) { this.rolesCsv = rolesCsv; }

    public Set<String> rolesAsSet() {
        if (rolesCsv == null || rolesCsv.isBlank()) return new LinkedHashSet<>();
        return new LinkedHashSet<>(Arrays.stream(rolesCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.replace("ROLE_", "")) // normalize
                .map(String::toUpperCase)
                .toList());
    }

    public static UserRequest fromEntity(User u) {
        var r = new UserRequest();
        r.setUsername(u.getUsername());
        r.setFullName(u.getFullName());
        r.setEmail(u.getEmail());
        r.setEnabled(u.isEnabled());
        r.setRolesCsv(String.join(", ", u.getRoles()));
        return r;
    }
}