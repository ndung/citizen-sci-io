package io.sci.citizen.model.dto;

import io.sci.citizen.model.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class ProjectRequest {

    private Long id;
    private boolean enabled = true;

    private String icon;

    private MultipartFile iconFile;

    private boolean publiclyAvailable = false;
    @NotBlank @NotNull
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    // getters/setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public MultipartFile getIconFile() {
        return iconFile;
    }

    public void setIconFile(MultipartFile iconFile) {
        this.iconFile = iconFile;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isPubliclyAvailable() {
        return publiclyAvailable;
    }

    public void setPubliclyAvailable(boolean publiclyAvailable) {
        this.publiclyAvailable = publiclyAvailable;
    }

    public Project toEntity() {
        Project p = new Project();
        p.setName(name != null ? name.trim() : null);
        p.setEnabled(enabled);
        p.setIcon(icon != null ? icon.trim() : null);
        p.setDescription(description);
        p.setPubliclyAvailable(publiclyAvailable);
        return p;
    }
}
