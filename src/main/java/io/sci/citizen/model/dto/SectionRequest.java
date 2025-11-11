package io.sci.citizen.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SectionRequest {

    private Long id;

    @NotNull
    private Integer sequence;

    @NotBlank
    private String type;

    @NotBlank
    private String name;

    private boolean enabled = true;

    // nullable = not tied to any project
    private Long projectId;

}