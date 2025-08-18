package io.sci.citizen.api;

import io.sci.citizen.service.ProjectService;
import io.sci.citizen.model.dto.ProjectResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectApiController {
    private final ProjectService service;
    public ProjectApiController(ProjectService service) { this.service = service; }

    @GetMapping
    public List<ProjectResponse> all() {
        return service.findAll().stream().map(ProjectResponse::from).toList();
    }
}
