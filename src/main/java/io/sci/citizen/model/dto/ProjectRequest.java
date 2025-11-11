package io.sci.citizen.model.dto;

import io.sci.citizen.model.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
