package io.sci.citizen.model.dto;

import io.sci.citizen.model.Menu;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class MenuRequest {
    @NotBlank @Size(max = 120)
    private String title;

    @Size(max = 255)
    private String href; // optional

    @Size(max = 80)
    private String icon;

    private Integer orderIndex = 0;

    private boolean enabled = true;

    private Long parentId; // nullable

    // CSV roles in the form; convert to set
    private String rolesCsv;

    // getters/setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getHref() { return href; }
    public void setHref(String href) { this.href = href; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getRolesCsv() { return rolesCsv; }
    public void setRolesCsv(String rolesCsv) { this.rolesCsv = rolesCsv; }

    public Set<String> rolesAsSet() {
        if (rolesCsv == null || rolesCsv.isBlank()) return new LinkedHashSet<>();
        return new LinkedHashSet<>(Arrays.stream(rolesCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.replace("ROLE_", "")) // normalize input
                .toList());
    }

    public static MenuRequest fromEntity(Menu e) {
        MenuRequest r = new MenuRequest();
        r.setTitle(e.getTitle());
        r.setHref(e.getHref());
        r.setIcon(e.getIcon());
        r.setOrderIndex(e.getOrderIndex());
        r.setEnabled(e.isEnabled());
        r.setParentId(e.getParent() != null ? e.getParent().getId() : null);
        r.setRolesCsv(String.join(", ", e.getRequiredRoles()));
        return r;
    }
}