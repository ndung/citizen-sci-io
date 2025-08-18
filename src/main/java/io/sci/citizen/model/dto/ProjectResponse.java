package io.sci.citizen.model.dto;

import io.sci.citizen.model.Project;

public class ProjectResponse {
    public Long id;
    public String name;
    public String description;

    public static ProjectResponse from(Project p) {
        var r = new ProjectResponse();
        r.id = p.getId();
        r.name = p.getName();
        r.description = p.getDescription();
        return r;
    }
}
